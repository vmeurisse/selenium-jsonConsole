package org.meurisse.selenium.jsonConsole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.ByteStreams;

public class JsonConsole extends RegistryBasedServlet {

  private static final long serialVersionUID = -3915127135814116576L;
  private static final Logger log = Logger.getLogger(JsonConsole.class.getName());
  private static String coreVersion;
  private static String coreRevision;

  public JsonConsole() {
    this(null);
  }

  public JsonConsole(Registry registry) {
    super(registry);
    getVersion();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(request, response);
  }

  protected void process(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(200);
    
    JSONObject jsonResponse = new JSONObject();
    
    try {
      jsonResponse.put("version", coreVersion + coreRevision);
      
      jsonResponse.put("nodes", new JSONArray());
      for (RemoteProxy proxy : getRegistry().getAllProxies()) {
        jsonResponse.append("nodes", getProxyJson(proxy));
      }
      
      jsonResponse.put("waiting", new JSONArray());
      Iterable<DesiredCapabilities> caps = getRegistry().getDesiredCapabilities();
      for (DesiredCapabilities desiredCapabilities : caps) {
        jsonResponse.append("waiting", desiredCapabilities.asMap());
      }
      
      jsonResponse.put("config", getRegistry().getConfiguration().getAllParams());
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    InputStream in = new ByteArrayInputStream(jsonResponse.toString().getBytes("UTF-8"));
    try {
      ByteStreams.copy(in, response.getOutputStream());
    } finally {
      in.close();
      response.flushBuffer();
    }
  }

  private JSONObject getProxyJson(RemoteProxy proxy) {
    JSONObject json = new JSONObject();
    try {
      json.put("id", proxy.getId());
      json.put("type", proxy.getClass().getSimpleName());
      json.put("status", proxy.getStatus());
      json.put("config", proxy.getConfig());
      
      Map <String, JSONObject> browsersMap = new HashMap <String, JSONObject>();
      for (TestSlot slot : proxy.getTestSlots()) {
        String proto = slot.getProtocol().isSelenium() ? "rc" : "webdriver";
        JSONArray browsers = json.optJSONArray(proto);
        if (browsers == null) {
          browsers = new JSONArray();
          json.put(proto, browsers);
        }
        String browserKey = proto + slot.getCapabilities();
        
        JSONObject browser = browsersMap.get(browserKey);
        if (browser == null) {
          browser = new JSONObject();
          putAll(browser, slot.getCapabilities());
          
          browsersMap.put(browserKey, browser);
          browsers.put(browser);
          
          browser.put("sessions", new JSONArray());
        }
        TestSession session = slot.getSession();
        if (session != null) {
          browser.accumulate("sessions", session.toString());
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    return json;
  }
  
  private void putAll(JSONObject json, Map<String, ?> map) throws JSONException {
    for (String key : map.keySet()) {
      json.put(key, map.get(key));
    }
  }
  
  private void getVersion() {
    final Properties p = new Properties();
    InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("VERSION.txt");
    if (stream == null) {
      log.severe("Couldn't determine version number");
      return;
    }
    try {
      p.load(stream);
    } catch (IOException e) {
      log.severe("Cannot load version from VERSION.txt" + e.getMessage());
    }
    coreVersion = p.getProperty("selenium.core.version");
    coreRevision = p.getProperty("selenium.core.revision");
    if (coreVersion == null) {
      log.severe("Cannot load selenium.core.version from VERSION.txt");
    }
  }
}
