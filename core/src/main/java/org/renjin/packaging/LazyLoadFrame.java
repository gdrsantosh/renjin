package org.renjin.packaging;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;

public class LazyLoadFrame {
  
  private static final int VERSION = 1;
  
  private class Value {
    private SEXP exp;
    private byte[] serialized;
    
    public Value(byte[] bytes) {
      serialized = bytes;
    }

    public SEXP get() {
      if(exp == null) {
        try {
          RDataReader reader = new RDataReader(context, 
              new GZIPInputStream(
                  new ByteArrayInputStream(serialized)));
          exp = reader.readFile();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        serialized = null;
      }
      return exp;
    }
  }
  
  private Context context;
  private Map<Symbol, Value> map = Maps.newIdentityHashMap();

  public LazyLoadFrame(Context context, InputSupplier<? extends InputStream> frame) throws IOException {
    this.context = context;
    
    DataInputStream din = new DataInputStream(frame.getInput());
    int version = din.readInt();
    if(version != VERSION) {
      throw new IOException("Unsupported version: " + version);
    }
    int count = din.readInt();
    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int byteCount = din.readInt();
      byte[] bytes = new byte[byteCount];
      din.readFully(bytes);
      map.put(Symbol.get(name), new Value(bytes));
    }
    din.close();
  }
  
  public Iterable<Symbol> getNames() {
    return map.keySet();
  }
  
  public SEXP get(Symbol name) {
    return map.get(name).get();
  }
  
  public static Iterable<NamedValue> load(Context context, InputSupplier<InputStream> input) throws IOException {
    LazyLoadFrame frame = new LazyLoadFrame(context, input);
    List<NamedValue> values = Lists.newArrayList();
    for(Symbol name : frame.getNames()) {
      values.add(new PackageValue(name, frame.get(name)));
    }
    return values;
  }

  private static class PackageValue implements NamedValue {
    private Symbol name;
    private SEXP value;
    
    public PackageValue(Symbol name, SEXP value) {
      super();
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean hasName() {
      return true;
    }

    @Override
    public String getName() {
      return name.getPrintName();
    }

    @Override
    public SEXP getValue() {
      return value;
    } 
  }
}
