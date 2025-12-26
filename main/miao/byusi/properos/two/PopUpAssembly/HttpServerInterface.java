package miao.byusi.properos.two.PopUpAssembly;

import android.content.Context;

public interface HttpServerInterface {
	void startServer(Context context, String host, int port, String webRoot, String defaultDocument);
	void stopServer();
	boolean isServerRunning();
	String getServerUrl();
}