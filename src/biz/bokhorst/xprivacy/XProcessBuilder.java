package biz.bokhorst.xprivacy;

import java.io.IOException;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class XProcessBuilder extends XHook {

	private String mCommand;

	public XProcessBuilder(String methodName, String restrictionName, String[] permissions, String command) {
		super(methodName, restrictionName, permissions, command);
		mCommand = command;
	}

	// public Process start()
	// libcore/luni/src/main/java/java/lang/ProcessBuilder.java
	// http://developer.android.com/reference/java/lang/ProcessBuilder.html

	@Override
	protected void before(MethodHookParam param) throws Throwable {
		String methodName = param.method.getName();
		if (methodName.equals("start")) {
			// Get commands
			ProcessBuilder builder = (ProcessBuilder) param.thisObject;
			List<String> listProg = builder.command();

			// Check commands
			if (listProg != null) {
				String command = TextUtils.join(" ", listProg);
				if ((mCommand == null && !command.contains("sh ") && !command.contains("su "))
						|| (mCommand != null && command.contains(mCommand + " ")))
					if (isRestricted(param, mCommand == null ? getMethodName() : mCommand))
						param.setThrowable(new IOException());
			}
		} else
			Util.log(this, Log.WARN, "Unknown method=" + methodName);
	}

	@Override
	protected void after(MethodHookParam param) throws Throwable {
		// Do nothing
	}
}
