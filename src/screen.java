package miao.byusi.properos.two;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import miao.byusi.properos.two.DeviceDisplay;
import miao.byusi.properos.two.MyUpnpService;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.connectionmanager.callback.PrepareForConnection;
import org.teleal.cling.support.model.ProtocolInfos;


public class screen {

	private Dialog listdialog;
	private ListView devicelist;
	private ArrayAdapter<DeviceDisplay> listAdapter;
	private AndroidUpnpService upnpService;
	private RegistryListener registryListener;
	private ServiceConnection serviceConnection;
	private String s = "AVTransport";
	private String s1 = "ConnectionManager";
	String url = "http://gcqq450f71eywn6bv7u.exp.bcevod.com/mda-hbqagik5sfq1jsai/mda-hbqagik5sfq1jsai.mp4";
	private int deviceIndex = 0;


	public int 初始化(final android.app.Activity ei) {


		registryListener = new BrowseRegistryListener();
		serviceConnection = new ServiceConnection(){

			public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
				upnpService = (AndroidUpnpService)iBinder;
				for (Device device :upnpService.getRegistry().getDevices()) {
					((BrowseRegistryListener)registryListener).deviceAdded(device,ei);
				}
				upnpService.getRegistry().addListener(registryListener);
				upnpService.getControlPoint().search();
			}

			public void onServiceDisconnected(ComponentName componentName) {
				upnpService = null;
			}
		};
		return 0;
	}


	public int 搜索设备(Context ei){

		showDialog(ei);

		return 2;

	}


	public void  暂停播放() {

		DeviceDisplay deviceDisplay = (DeviceDisplay)listAdapter.getItem(deviceIndex);
		Device device = deviceDisplay.getDevice();
		executePause(device);

	}


	public void  继续播放() {

		DeviceDisplay deviceDisplay = (DeviceDisplay)listAdapter.getItem(deviceIndex);
		Device device = deviceDisplay.getDevice();
		executePlay(device);

	}


	public void  停止播放() {

		DeviceDisplay deviceDisplay = (DeviceDisplay)listAdapter.getItem(deviceIndex);
		Device device = deviceDisplay.getDevice();
		executeStop(device);

	}


	public void showDialog(Context ei) {
		  Log.d("showDialog", "UPnP服务为空");
		AlertDialog.Builder builder = new AlertDialog.Builder(ei);
		builder.setTitle("可选择设备……");

		// 创建列表视图
		ListView devicelist = new ListView(ei);
		final ArrayAdapter<DeviceDisplay> listAdapter = new ArrayAdapter<>(ei, android.R.layout.simple_list_item_1);
		devicelist.setAdapter(listAdapter);

		// 绑定服务
		ei.bindService(new Intent(ei, MyUpnpService.class), serviceConnection, Context.BIND_AUTO_CREATE);

		// 设置列表视图到对话框
		builder.setView(devicelist);

		// 设置取消按钮
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int n) {
					dialogInterface.dismiss();
				}
			});
			
			
			// 创建对话框
		final AlertDialog dialog = builder.create();
		dialog.show();

		// 列表项点击事件
		devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int n, long l) {
					deviceIndex = n;
					DeviceDisplay deviceDisplay = listAdapter.getItem(n);
					Device device = deviceDisplay.getDevice();
					Uri.parse(url);
					GetInfo(device);
					executeAVTransportURI(device, url);
					executePlay(device);
					dialog.dismiss();
						}

				
			});
	}

	

	public void  置投屏幕内容(String 播放地址) {

		url = 播放地址;

	}

	public void onDestroy(Context ei) {
		if (upnpService != null) {
			upnpService.getRegistry().removeListener(registryListener);
		}
		if (serviceConnection != null) {
			ei.unbindService(serviceConnection);
		}
	}


	public void executeAVTransportURI(Device device, String string) {
		UDAServiceId uDAServiceId = new UDAServiceId(s);
		Service service = device.findService((ServiceId)uDAServiceId);
		SetAVTransportURI setAVTransportURI = new SetAVTransportURI(service, string){

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.e((String)"SetAVTransportURI", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)setAVTransportURI);
	}


	public void executePlay(Device device) {
		UDAServiceId uDAServiceId = new UDAServiceId(s);
		Service service = device.findService((ServiceId)uDAServiceId);
		Play play = new Play(service){

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.e((String)"Play", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)play);
	}


	public void executePause(Device device) {
		UDAServiceId uDAServiceId = new UDAServiceId(s);
		Service service = device.findService((ServiceId)uDAServiceId);
		Pause pause = new Pause(service){

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.e((String)"Play", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)pause);
	}


	public void executeStop(Device device) {
		UDAServiceId uDAServiceId = new UDAServiceId(s);
		Service service = device.findService((ServiceId)uDAServiceId);
		Stop stop = new Stop(service){

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.e((String)"Play", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)stop);
	}


	public void executeSeek(Device device, String string) {
		UDAServiceId uDAServiceId = new UDAServiceId(s);
		Service service = device.findService((ServiceId)uDAServiceId);
		Seek seek = new Seek(service, string){

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.e((String)"Play", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)seek);
	}



	public void GetInfo(Device device) {
		UDAServiceId uDAServiceId = new UDAServiceId(s1);
		Service service = device.findService((ServiceId)uDAServiceId);
		GetProtocolInfo getProtocolInfo = new GetProtocolInfo(service){

			public void received(ActionInvocation actionInvocation, ProtocolInfos protocolInfos, ProtocolInfos protocolInfos2) {
				Log.v((String)"sinkProtocolInfos", (String)protocolInfos.toString());
				Log.v((String)"sourceProtocolInfos", (String)protocolInfos2.toString());
			}

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.v((String)"GetProtocolInfo", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)getProtocolInfo);
	}



	public void PrepareConn(Device device) {
		UDAServiceId uDAServiceId = new UDAServiceId(s1);
		Service service = device.findService((ServiceId)uDAServiceId);
		PrepareForConnection prepareForConnection = new PrepareForConnection(service, null, null, - 1, null){

			public void received(ActionInvocation actionInvocation, int n, int n2, int n3) {
				Log.v((String)"avTransportID", (String)Integer.toString(n3));
			}

			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String string) {
				Log.v((String)"PrepareForConnection", (String)"failed^^^^^^^");
			}
		};
		upnpService.getControlPoint().execute((ActionCallback)prepareForConnection);
	}


	class BrowseRegistryListener
	extends DefaultRegistryListener {
		BrowseRegistryListener() {
		}

		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice remoteDevice,android.app.Activity ei) {
			deviceAdded((Device)remoteDevice,ei);
		}

		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice remoteDevice, Exception exception,android.app.Activity ei) {
			deviceRemoved((Device)remoteDevice,ei);
		}

		public void remoteDeviceAdded(Registry registry, RemoteDevice remoteDevice,android.app.Activity ei) {
			deviceAdded((Device)remoteDevice,ei);
		}

		public void remoteDeviceRemoved(Registry registry, RemoteDevice remoteDevice,android.app.Activity ei) {
			deviceRemoved((Device)remoteDevice,ei);
		}

		public void localDeviceAdded(Registry registry, LocalDevice localDevice,android.app.Activity ei) {
			deviceAdded((Device)localDevice,ei);
		}

		public void localDeviceRemoved(Registry registry, LocalDevice localDevice,android.app.Activity ei) {
			deviceRemoved((Device)localDevice,ei);
		}

		public void deviceAdded(final Device device,android.app.Activity ei) {
			ei.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						DeviceDisplay deviceDisplay = new DeviceDisplay(device);
						int n = listAdapter.getPosition(deviceDisplay);
						if (n >= 0) {
							listAdapter.remove(deviceDisplay);
							listAdapter.insert(deviceDisplay, n);
						} else {
							listAdapter.add(deviceDisplay);
						}
						listAdapter.notifyDataSetChanged();
					}
				});
		}

		public void deviceRemoved(final Device device,android.app.Activity ei) {
			ei.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						listAdapter.remove(new DeviceDisplay(device));
					}
				});
		}
	}
}