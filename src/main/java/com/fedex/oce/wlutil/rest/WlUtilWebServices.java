package com.fedex.oce.wlutil.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fedex.oce.wlutil.rest.ExecutionEnum.ExecutionCd;

@Path("/wlutil")
public class WlUtilWebServices {

	private final Logger LOG = LoggerFactory.getLogger(WlUtilWebServices.class);

	private Properties props = new Properties();

	private String wlServerTmpFolder;
	private String wlServerLogsFolder;

	public WlUtilWebServices() {
		try {
			InputStream in = WlUtilWebServices.class.getClassLoader().getResourceAsStream("appcodes.properties");
			props.load(in);
			wlServerTmpFolder = props.getProperty("wl.adminserver.tmp");
			wlServerLogsFolder = props.getProperty("wl.adminserver.logs");
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@GET
	@Path("/clean")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clearTempFolder(@QueryParam("clearLogs") boolean clearLogs,
			@QueryParam("delTempFiles") boolean delTempFiles) {
		LOG.info("wlServer Tmp Folder =>" + wlServerTmpFolder);
		LOG.info("wlServer Logs Folder =>" + wlServerLogsFolder);
		LOG.info("Clear Logs =>" + clearLogs + ", Delete Tmp Folder =>" + delTempFiles);

		Map<String, ResultInfo> results = cleanFolders(clearLogs, delTempFiles);

		return Response.status(Status.OK).entity(results).build();
	}

	private Map<String, ResultInfo> cleanFolders(boolean clearLogs, boolean delTempFiles) {
		Map<String, ResultInfo> resultsMap = new HashMap<>();

		if (clearLogs || delTempFiles) {
			ResultInfo resultInfo = null;
			if (clearLogs) {
				resultInfo = executeCommand("rm -rf " + wlServerLogsFolder);
				resultsMap.put("clearLogs", resultInfo);
			}

			if (delTempFiles) {
				resultInfo = executeCommand("rm -rf " + wlServerTmpFolder);
				resultsMap.put("delTempFiles", resultInfo);
			}
			// LOG.debug(gsonObj.toJson(resultInfo));

		}
		return resultsMap;
	}

	private ResultInfo executeCommand(String command) {
		LOG.debug("command =>" + command);

		String errorStr = null;
		String outputStr = null;

		ResultInfo resultInfo = null;

		if (null != command && command.length() > 0) {
			try {
				Runtime run = Runtime.getRuntime();
				Process procObj = run.exec(command);

				InputStream stdInp = procObj.getInputStream();
				InputStreamReader isrInp = new InputStreamReader(stdInp);
				BufferedReader brInp = new BufferedReader(isrInp);

				InputStream stderr = procObj.getErrorStream();
				InputStreamReader isrErr = new InputStreamReader(stderr);
				BufferedReader brErr = new BufferedReader(isrErr);

				if (null != brErr.readLine()) {
					StringBuffer errSb = new StringBuffer("<ERROR>");
					String line;
					while ((line = brErr.readLine()) != null) {
						errSb.append(line);
					}
					errSb.append("</ERROR>");
					errorStr = errSb.toString();
				}

				if (null != brInp.readLine()) {
					StringBuffer outSb = new StringBuffer("<OUTPUT>");
					String line = null;
					while ((line = brInp.readLine()) != null) {
						outSb.append(line);
					}
					outSb.append("</OUTPUT>");
					outputStr = outSb.toString();
				}

				int exitVal = procObj.waitFor();
				LOG.info("Process exitValue: " + exitVal);
				if (exitVal == 0) {
					// LOG.info(outputStr);
					resultInfo = new ResultInfo(ExecutionCd.SUCCESS, outputStr != null ? outputStr.toString() : "");
				} else {
					LOG.info(errorStr);
					resultInfo = new ResultInfo(ExecutionCd.FAILURE, errorStr.toString());
				}

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultInfo;
	}

}