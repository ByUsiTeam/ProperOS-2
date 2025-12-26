package miao.byusi.properos.two.PopUpAssembly;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class HttpServerManager {
	private AndroidHttpServer server;
	private String host = "0.0.0.0";
	private int port = 8080;
	private String webRoot;
	private String defaultDocument = "index.html";
	
	public HttpServerManager setHost(String host) {
		this.host = host;
		return this;
	}
	
	public HttpServerManager setPort(int port) {
		this.port = port;
		return this;
	}
	
	public HttpServerManager setWebRoot(String webRoot) {
		this.webRoot = webRoot;
		return this;
	}
	
	public HttpServerManager setDefaultDocument(String defaultDocument) {
		this.defaultDocument = defaultDocument;
		return this;
	}
	
	public HttpServerManager useAppPrivateDir(Context context) {
		File dir = new File(context.getFilesDir(), "www");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.webRoot = dir.getAbsolutePath();
		return this;
	}
	
	public HttpServerManager useExternalDir(String subDir) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File externalDir = Environment.getExternalStorageDirectory();
			File dir = new File(externalDir, subDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			this.webRoot = dir.getAbsolutePath();
		}
		return this;
	}
	
	public boolean start(Context context) {
		if (webRoot == null) {
			useAppPrivateDir(context);
		}
		
		File dir = new File(webRoot);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		try {
			server = new AndroidHttpServer(host, port, webRoot, defaultDocument);
			server.start();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void stop() {
		if (server != null && server.isAlive()) {
			server.stop();
		}
	}
	
	public boolean isRunning() {
		return server != null && server.isAlive();
	}
	
	public String getLocalUrl() {
		return "http://localhost:" + port;
	}
}