package miao.byusi.properos.two.PopUpAssembly;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AndroidHttpServer extends NanoHTTPD {
	private final String webRoot;
	private final String defaultDocument;
	
	private static final Map<String, String> MIME_TYPES = new HashMap<>();
	
	static {
		MIME_TYPES.put("html", "text/html; charset=utf-8");
		MIME_TYPES.put("htm", "text/html; charset=utf-8");
		MIME_TYPES.put("css", "text/css; charset=utf-8");
		MIME_TYPES.put("js", "application/javascript; charset=utf-8");
		MIME_TYPES.put("json", "application/json; charset=utf-8");
		MIME_TYPES.put("xml", "application/xml; charset=utf-8");
		MIME_TYPES.put("txt", "text/plain; charset=utf-8");
		MIME_TYPES.put("jpg", "image/jpeg");
		MIME_TYPES.put("jpeg", "image/jpeg");
		MIME_TYPES.put("png", "image/png");
		MIME_TYPES.put("gif", "image/gif");
		MIME_TYPES.put("ico", "image/x-icon");
		MIME_TYPES.put("svg", "image/svg+xml");
		MIME_TYPES.put("pdf", "application/pdf");
		MIME_TYPES.put("zip", "application/zip");
		MIME_TYPES.put("mp3", "audio/mpeg");
		MIME_TYPES.put("mp4", "video/mp4");
	}
	
	public AndroidHttpServer(String host, int port, String webRoot, String defaultDocument) {
		super(host, port);
		this.webRoot = new File(webRoot).getAbsolutePath();
		this.defaultDocument = defaultDocument;
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		
		if (uri.equals("/") || uri.isEmpty() || uri.endsWith("/")) {
			uri += defaultDocument;
		}
		
		String filePath = webRoot + uri;
		File file = new File(filePath);
		
		if (!file.getAbsolutePath().startsWith(webRoot)) {
			return errorResponse(Response.Status.FORBIDDEN, "Forbidden");
		}
		
		if (!file.exists() || file.isDirectory()) {
			File parentDir = file.isDirectory() ? file : file.getParentFile();
			if (parentDir != null) {
				file = new File(parentDir, defaultDocument);
			}
		}
		
		if (!file.exists() || !file.isFile()) {
			return errorResponse(Response.Status.NOT_FOUND, "Not Found: " + uri);
		}
		
		if (!file.canRead()) {
			return errorResponse(Response.Status.FORBIDDEN, "Forbidden");
		}
		
		try {
			String mimeType = getMimeType(file.getName());
			FileInputStream fis = new FileInputStream(file);
			
			Response response = newFixedLengthResponse(
				Response.Status.OK, 
				mimeType, 
				fis, 
				file.length()
			);
			
			response.addHeader("Cache-Control", "no-cache");
			response.addHeader("Access-Control-Allow-Origin", "*");
			
			return response;
			
		} catch (IOException e) {
			return errorResponse(Response.Status.INTERNAL_ERROR, "Internal Error");
		}
	}
	
	private Response errorResponse(Response.Status status, String message) {
		String html = "<html><body><h1>" + status.getRequestStatus() + "</h1><p>" + message + "</p></body></html>";
		return newFixedLengthResponse(status, "text/html; charset=utf-8", html);
	}
	
	private String getMimeType(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
			String ext = fileName.substring(dotIndex + 1).toLowerCase();
			return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
		}
		return "application/octet-stream";
	}
	
	@Override
	public void start() throws IOException {
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
	}
}