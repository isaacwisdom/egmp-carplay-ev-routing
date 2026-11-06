package org.egmp.carplay.ev;
// SPDX-License-Identifier: GPL-3.0-or-later
import java.io.File;
import hmgapi.hook.*;
/** EV routing provider for HMG-API beta6 and newer. */
public final class EvRouting extends ModEntry {
    private String profile;
    private boolean librariesMatch;
    private String sessionMd5;
    private String clientMd5;
    private ModContext runningMod;
    private Class<?> part;
    public void zygoteInit(ModContext mod) throws Throwable {
        sessionMd5=ModContext.md5("/system/lib/libiap2session.so");
        clientMd5=ModContext.md5("/system/lib/libiap2client.so");
        librariesMatch="fc7cb675b46a44bd893a1edc054c5e71".equals(sessionMd5)
            && "153a97fd6d2c62dc58286aa47c35feb3".equals(clientMd5);
        // Read before dropping root: app processes cannot traverse the mod folder.
        File f=new File("/system/etc/carplay.ev-routing/profile.json");
        java.io.InputStream in=new java.io.FileInputStream(f);
        try {java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){if(out.size()+n>32768)throw new java.io.IOException("profile too large");out.write(b,0,n);}profile=new String(out.toByteArray(),"UTF-8");
        } finally {in.close();}
    }
    public void appLoaded(final ModContext mod,final LoadedApp app) throws Throwable {
        if(!"com.tcc.iap2server".equals(app.packageName))return;
        if(!librariesMatch){mod.error("CarPlayEV: unsupported native hashes; inactive"
            +" libiap2session.so_md5="+(sessionMd5==null?"unavailable":sessionMd5)
            +" libiap2client.so_md5="+(clientMd5==null?"unavailable":clientMd5),null);return;}
        String[] points={"iap2.service.on_create","iap2.service.on_destroy","iap2.service.on_bind","iap2.service.native_handle","iap2.service.native_lock","iap2.init_info.check_supports_features","iap2.init_info.use_vehicle_information","iap2.init_info.use_vehicle_status"};
        for(String id:points)if(!Points.has(id)){mod.error("CarPlayEV: missing hook map "+id+"; inactive",null);return;}
        mod.hook(app.classLoader,"iap2.init_info.check_supports_features",new MethodHook(){
            protected void afterHookedMethod(Param p) throws Throwable {enableGroups(app.classLoader);}
        });
        enableGroups(app.classLoader);
        runningMod=mod;part=mod.appPart(app,"org.egmp.carplay.ev.app.AppProvider");
        mod.nativePatch(app,"libiap2session.so");
        mod.hook(app.classLoader,"iap2.service.on_create",new MethodHook(){
            protected void afterHookedMethod(Param p) throws Throwable {
                if(p.hasThrowable())return;
                // All native calls must resolve in the app-part loader.
                Class<?> bridge=mod.appPart(app,"org.egmp.carplay.ev.app.NativeBridge");
                bridge.getMethod("load",String.class).invoke(null,"/system/lib/libcarplayev.so");
                part.getMethod("start",ModContext.class,LoadedApp.class,Object.class,String.class).invoke(null,mod,app,p.thisObject,profile);
            }
        });
        mod.hook(app.classLoader,"iap2.service.on_destroy",new MethodHook(){
            protected void beforeHookedMethod(Param p) throws Throwable {stop();}
        });
    }
    private static void enableGroups(ClassLoader cl) throws Exception {
        Points.field(cl,"iap2.init_info.use_vehicle_information").setInt(null,1);
        Points.field(cl,"iap2.init_info.use_vehicle_status").setInt(null,1);
    }
    private void stop() throws Throwable {if(part!=null)part.getMethod("stop").invoke(null);}
    public void onEnabled(ModContext mod,boolean enabled) throws Throwable {
        if(!enabled && runningMod==mod)stop();
        // Re-enable requires service restart so a new stock listener can register.
    }
}
