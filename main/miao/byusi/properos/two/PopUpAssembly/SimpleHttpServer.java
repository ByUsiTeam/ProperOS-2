package miao.byusi.properos.two.PopUpAssembly;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class SimpleHttpServer {
	private NanoHTTPD server;
	private String webRoot;
	private String defaultDoc;
	
	public SimpleHttpServer(String webRoot, String defaultDoc) {
		this.webRoot = webRoot;
		this.defaultDoc = defaultDoc;
	}
	
	public void start(final String host, final int port) throws IOException {
		if (server != null && server.isAlive()) {
			return;
		}
		
		server = new NanoHTTPD(host, port) {
			@Override
			public Response serve(IHTTPSession session) {
				String uri = session.getUri();
				
				if (uri.equals("/") || uri.isEmpty()) {
					uri = "/" + defaultDoc;
				}
				
				File file = new File(webRoot + uri);
				
				if (!file.exists() || !file.isFile()) {
					return newFixedLengthResponse(
						Response.Status.NOT_FOUND, 
						"text/html", 
						"<h1>404 Not Found</h1>"
					);
				}
				
				try {
					String mime = getMimeType(file.getName());
					return newFixedLengthResponse(
						Response.Status.OK, 
						mime, 
						new java.io.FileInputStream(file), 
						file.length()
					);
				} catch (Exception e) {
					return newFixedLengthResponse(
						Response.Status.INTERNAL_ERROR, 
						"text/html", 
						"<h1>500 Error</h1>"
					);
				}
			}
			
			private String getMimeType(String fileName) {
				if (fileName.endsWith(".html")) return "text/html";
				if (fileName.endsWith(".css")) return "text/css";
				if (fileName.endsWith(".js")) return "application/javascript";
				if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
				if (fileName.endsWith(".png")) return "image/png";
				return "application/octet-stream";
			}
		};
		
		server.start();
	}
	
	public void stop() {
		if (server != null) {
			server.stop();
		}
	}
}