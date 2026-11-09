package org.egmp.carplay.ev.app;
// SPDX-License-Identifier: GPL-3.0-or-later
import org.json.*;
import java.nio.file.*;
import java.util.Arrays;
public final class ProducerHost {
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
    public static void main(String[] args) throws Exception {
        JSONArray fixtures=new JSONArray(new String(Files.readAllBytes(Paths.get(args[0])),"UTF-8"));
        for(int i=0;i<fixtures.length();i++){
            JSONObject f=fixtures.getJSONObject(i);JSONArray s=f.getJSONArray("sample");int[] expected=new int[9];for(int n=0;n<9;n++)expected[n]=s.getInt(n);
            VehicleSource.Snapshot actual=VehicleSource.encode(expected[0],expected[1],f.getString("charging"),f.getString("consumption"),expected[7]==1?Double.valueOf(expected[3]/1000.0):null,expected[7]==1?Integer.valueOf(expected[4]):null,null,false);
            check(Arrays.equals(expected,actual.values),f.getString("name"));
            check(new JSONObject(new String(actual.charging,"UTF-8")).toString().equals(new JSONObject(f.getString("charging")).toString()),"charging");
            check(new JSONObject(new String(actual.consumption,"UTF-8")).toString().equals(new JSONObject(f.getString("consumption")).toString()),"consumption");
        }
        check(VehicleSource.auxiliary(0,31,0,0)==310,"observed auxiliary");
        check(VehicleSource.auxiliary(26,24,10,20)==2960,"mixed auxiliary");
        check(VehicleSource.auxiliary(0,0,0,0)==0,"zero auxiliary");
        check(VehicleSource.displayedSoc(0)==0 && VehicleSource.displayedSoc(0.5)==0,"floor zero half-step");
        check(VehicleSource.displayedSoc(74)==74 && VehicleSource.displayedSoc(74.5)==74,"floor display SOC");
        check(VehicleSource.displayedSoc(99.5)==99 && VehicleSource.displayedSoc(100)==100,"floor upper half-step");
        for(double bad:new double[]{-0.5,100.5,Double.NaN,Double.POSITIVE_INFINITY})try{VehicleSource.displayedSoc(bad);throw new AssertionError("accepted invalid SOC");}catch(IllegalArgumentException expected){}
        String profile=new String(Files.readAllBytes(Paths.get(args[1])),"UTF-8");
        new VehicleSource(null,profile);
        JSONObject p=new JSONObject(profile);
        String ch=p.getJSONObject("charging_contract").toString(),co=p.getJSONObject("consumption_contract").toString();
        VehicleSource.Snapshot held=VehicleSource.encode(0,77400,ch,co,Double.valueOf(50),Integer.valueOf(206),Integer.valueOf(310),false);
        check(held.auxiliarySource.equals("last_known") && held.auxiliaryW==310,"held auxiliary");
        check(VehicleSource.fresh(1,15000000001L) && !VehicleSource.fresh(1,15000000002L) && !VehicleSource.fresh(2,1),"sample freshness");
        Subscription gate=new Subscription();gate.connected(3);long a=gate.start();check(gate.current(a),"start");
        check(gate.start()==a,"duplicate start idempotent");check(!gate.disconnected(4) && gate.current(a),"unrelated component");
        gate.stop();check(!gate.current(a),"stop invalidates worker");long b=gate.start();check(b!=a && gate.current(b),"rapid resubscribe");
        gate.connected(5);check(!gate.current(b),"new transport invalidates old worker");long c=gate.start();check(gate.disconnected(5) && !gate.current(c),"matching disconnect");
        gate.close();check(gate.start()==0,"closed provider cannot restart");
        System.out.println("PASS nine producer reference fixtures, Hyundai SOC flooring, auxiliary scaling/retention, and semantic subscription lifecycle");
    }
}
