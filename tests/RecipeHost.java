// SPDX-License-Identifier: GPL-3.0-or-later
import hmgapi.engine.MemRecipe;
import java.nio.file.*;
import org.json.*;
import java.util.Arrays;
public final class RecipeHost {
 public static void main(String[] args)throws Exception {
  final byte[] lib=Files.readAllBytes(Paths.get(args[0]));final JSONArray writes=new JSONArray();
  MemRecipe r=MemRecipe.parse(new String(Files.readAllBytes(Paths.get(args[1])),"UTF-8"));
  MemRecipe.Memory memory=new MemRecipe.Memory(){
   public byte[] peek(long p,int n){return Arrays.copyOfRange(lib,(int)(p-0x1000000L),(int)(p-0x1000000L)+n);}
   public boolean poke(long p,byte[] data){JSONObject w=new JSONObject();w.put("address",p);StringBuilder hex=new StringBuilder();for(byte b:data)hex.append(String.format("%02x",b&255));w.put("hex",hex.toString());writes.put(w);if(p<0x1100000L)System.arraycopy(data,0,lib,(int)(p-0x1000000L),data.length);return true;}
   public long mapCode(long hint,int n){return hint;}
  };
  MemRecipe.Result result=r.apply(memory,0x1000000L,0x30000L,MemRecipe.md5(lib));
  if(!result.ok())throw new AssertionError(result.message);
  JSONObject out=new JSONObject();out.put("result",result.message);out.put("segment",result.segment);out.put("writes",writes);
  Files.write(Paths.get(args[2]),out.toString(2).getBytes("UTF-8"));
  // A wrong byte anywhere in a verify region must prevent every write.
  final int count=writes.length();lib[0x1aa04]^=1;
  if(r.apply(memory,0x1000000L,0x30000L,null).ok() || writes.length()!=count)throw new AssertionError("mismatch wrote memory");
  System.out.println("PASS real HMG-API MemRecipe applies gate and group13; unknown bytes cause no writes");
 }
}
