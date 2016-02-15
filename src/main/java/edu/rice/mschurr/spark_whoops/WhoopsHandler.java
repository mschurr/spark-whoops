package edu.rice.mschurr.spark_whoops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.IOUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class WhoopsHandler implements ExceptionHandler {
  protected final static Logger logger = LoggerFactory.getLogger(WhoopsHandler.class);
  
  /**
   * Installs the Whoops pretty-page handler for all Exceptions.
   */
  public static void install() {
    Spark.exception(Exception.class, new WhoopsHandler());
  }
  
  protected final FreeMarkerEngine templateEngine;
  protected final Configuration templateConfig;

  protected WhoopsHandler() {
    templateEngine = new FreeMarkerEngine();
    templateConfig = new Configuration();
    templateConfig.setTemplateLoader(new ClassTemplateLoader(WhoopsHandler.class, "."));
    templateEngine.setConfiguration(templateConfig);
  }
  
  @Override
  public final void handle(Exception exception, Request request, Response response) {
    try {
      List<Map<String, Object>> frames = parseFrames(exception);
      
      LinkedHashMap<String, Object> model = new LinkedHashMap<>();
      model.put("has_frames", !frames.isEmpty());
      model.put("frame_count", frames.size());
      model.put("message", Optional.fromNullable(exception.getMessage()).or(""));
      model.put("plain_exception", ExceptionUtils.getStackTrace(exception));
      model.put("frames", frames);
      model.put("name", exception.getClass().getCanonicalName().split("\\."));
      model.put("basic_type", exception.getClass().getSimpleName());
      model.put("type", exception.getClass().getCanonicalName());
      model.put("css", IOUtils.toString(new FileInputStream(new File(WhoopsHandler.class.getResource("whoops.css").toURI()))));
      model.put("zepto_js", IOUtils.toString(new FileInputStream(new File(WhoopsHandler.class.getResource("zepto.min.js").toURI()))));
      model.put("clipboard_js", IOUtils.toString(new FileInputStream(new File(WhoopsHandler.class.getResource("clipboard.min.js").toURI()))));
      model.put("whoops_js", IOUtils.toString(new FileInputStream(new File(WhoopsHandler.class.getResource("whoops.base.js").toURI()))));
      
      LinkedHashMap<String, Map<String, ? extends Object>> tables = new LinkedHashMap<>();
      installTables(tables, request, exception);
      model.put("tables", tables);
      
      response.body(templateEngine.render(Spark.modelAndView(model, "whoops.ftl")));
    } catch (Exception e) {
      response.body( 
          "<html>"
        + "  <body>"
        + "    <h1>Caught Exception:</h1>"
        + "    <pre>" + ExceptionUtils.getStackTrace(exception) + "</pre>"
        + "    <h1>Caught Exception Rendering Error Page:</h1>"
        + "    <pre>" + ExceptionUtils.getStackTrace(e) + "</pre>"
        + "  </body>"
        + "</html>"
      );
    }
  }
  
  /**
   * Install any tables you want to be shown in environment details.
   * @param tables
   */
  protected void installTables(LinkedHashMap<String, Map<String, ? extends Object>> tables, Request request, Exception exception) {
    LinkedHashMap<String, Object> environment = new LinkedHashMap<>();
    tables.put("Environment", environment);
    environment.put("Thread ID", Thread.currentThread().getId());

    LinkedHashMap<String, Object> req = new LinkedHashMap<>();
    tables.put("Request", req);
    req.put("User-Agent", Optional.fromNullable(request.userAgent()).or("-"));
    req.put("URI", Optional.fromNullable(request.uri()).or("-"));
    req.put("URL", Optional.fromNullable(request.url()).or("-"));
    req.put("Scheme", Optional.fromNullable(request.scheme()).or("-"));
    req.put("Method", Optional.fromNullable(request.requestMethod()).or("-"));
    req.put("Protocol", Optional.fromNullable(request.protocol()).or("-"));
    req.put("Port", Optional.fromNullable(Integer.toString(request.port())).or("-"));
    req.put("Path Info", Optional.fromNullable(request.pathInfo()).or("-"));
    req.put("Remote IP", Optional.fromNullable(request.ip()).or("-"));
    req.put("Host", Optional.fromNullable(request.host()).or("-"));
    req.put("Content Type", Optional.fromNullable(request.contentType()).or("-"));
    req.put("Content Length", request.contentLength() == -1 ? "-" : Integer.toString(request.contentLength()));
    req.put("Context Path", Optional.fromNullable(request.contextPath()).or("-"));
    req.put("Body", Optional.fromNullable(request.body()).or("-"));
    req.put("Attributes", "[" + Joiner.on(", ").join(Optional.fromNullable(request.attributes()).or(ImmutableSet.of())) + "]");
    
    LinkedHashMap<String, Object> session = new LinkedHashMap<>();
    tables.put("Session", session);
    for (String s : request.session().attributes()) {
      session.put(s, request.session().attribute(s).toString());
    }
    
    LinkedHashMap<String, Object> queryParams = new LinkedHashMap<>();
    tables.put("Query Parameters", queryParams);
    for (String s : request.queryParams()) {
      queryParams.put(s, request.queryParams(s));
    }

    tables.put("Cookies", request.cookies());
    
    tables.put("Route Parameters", request.params());
    
    LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
    tables.put("Headers", headers);
    for (String header : request.headers()) {
      headers.put(header, request.headers(header));
    }
  }
  
  /**
   * Parses all stack frames for an exception into a view model.
   * @param e An exception.
   * @return A view model for the frames in the exception.
   */
  private final List<Map<String, Object>> parseFrames(Exception e) {
    ImmutableList.Builder<Map<String, Object>> frames = ImmutableList.builder();
    
    for (StackTraceElement frame : e.getStackTrace()) {
      frames.add(parseFrame(frame));
    }
    
    return frames.build();
  }
  
  /**
   * Parses a stack frame into a view model.
   * @param sframe A stack trace frame.
   * @return A view model for the given frame in the template.
   */
  private final Map<String, Object> parseFrame(StackTraceElement sframe) {
    ImmutableMap.Builder<String, Object> frame = ImmutableMap.builder();
    frame.put("file", Optional.fromNullable(sframe.getFileName()).or("<#unknown>"));
    frame.put("class", Optional.fromNullable(sframe.getClassName()).or(""));
    frame.put("line", Optional.fromNullable(Integer.toString(sframe.getLineNumber())).or(""));
    frame.put("function", Optional.fromNullable(sframe.getMethodName()).or(""));
    frame.put("comments", ImmutableList.of());

    Optional<File> file = findFileForFrame(sframe);
    Optional<Map<Integer, String>> codeLines = fetchFileLines(file, sframe);
    
    if (codeLines.isPresent()) {
      // Write the starting line number (1-indexed).
      frame.put("code_start", Iterables.reduce(codeLines.get().keySet(), Integer.MAX_VALUE, (a,i) -> Math.min(a, i)) + 1);
      
      // Write the code as a single string, replacing empty lines with a " ".
      frame.put("code", Joiner.on("\n").join(Iterables.map(codeLines.get().values(), (x) -> x.length() == 0 ? " " : x)));
      
      // Write the canonical path.
      try {
        frame.put("canonical_path", file.get().getPath());
      } catch (Exception e) {
        // Ignore (just don't have the canonical path).
      }
    }
    
    return frame.build();
  }
  
  /**
   * Attempts to find the source code (.java) file corresponding to the 
   * class name given in the stack trace frame. Assumes that the source 
   * code file is present within the working dir in ./src/main/java.
   * @param frame A stack trace frame.
   * @return An optional file containing the .java source for the frame.
   */
  private final Optional<File> findFileForFrame(StackTraceElement frame) {
    // If the file has no frame attached, we can't find one (obviously).
    if (frame.getFileName() == null) {
      return Optional.absent();
    }
    
    // TODO(mschurr): Possible also support /src/test/java?
    File base = new File("./src/main/java");
    try {
      logger.debug("Base Path: " + base.getCanonicalPath());
    } catch (IOException e1) {
    }
    
    if (!base.exists()) {
      return Optional.absent();
    }
    
    // Find a list of all matching files (any files with the same name as the name provided
    // in the trace). We may not find a file because sometimes source files are just not
    // available (e.g. we only have .class files in compiled apps). Since the stack trace
    // only gives the file base name, we need to enumerate all possibilities.
    Collection<File> possibilities = FileUtils.listFiles(base, new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().equals(frame.getFileName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return name.equals(frame.getFileName());
      }
    }, TrueFileFilter.INSTANCE);
    
        
    if (possibilities.size() == 1) {
      // If there's one possibility, use it as the source file.
      File file = Iterables.first(possibilities);
      return Optional.of(file);
    } else if (possibilities.size() > 1) {
      // If there are multiple possibilities, use the class name to filter them down.
      // Assumes the directory structures matches the package name of the class.
      String className = frame.getClassName();
      
      if (className.indexOf('$') != -1) {
        className = className.substring(0, className.lastIndexOf('$'));
      }
      
      String path = Joiner.on(File.separatorChar).join(ImmutableList.of("src", "main", "java")) + 
          File.separatorChar + className.replace('.', File.separatorChar) + ".java";
      
      try {
        for (File file : possibilities) {
          if (file.getCanonicalPath().endsWith(path)) {
            return Optional.of(file);
          }
        }
      } catch (IOException e) {
        return Optional.absent();
      }
      
      return Optional.absent();
    } else {
      return Optional.absent();
    }
  }
  
  /**
   * Fetches the lines of the source file corresponding to a StackTraceElement (fetches 20 lines total
   * centered on the line number given in the trace).
   * @param file An optional text file.
   * @param frame A stack trace frame.
   * @return An optional map of line numbers to the content of the lines (not terminated with \n).
   */
  private final Optional<Map<Integer, String>> fetchFileLines(Optional<File> file, StackTraceElement frame) {
    // If no line number is given or no file exists, we can't fetch lines.
    if (!file.isPresent() || frame.getLineNumber() == -1) {
      return Optional.absent();
    }
    
    // Otherwise, fetch 20 lines centered on the number provided in the trace.
    ImmutableMap.Builder<Integer, String> lines = ImmutableMap.builder();
    int start = Math.max(frame.getLineNumber() - 10, 0);
    int end = start + 20;
    int current = 0;
    
    try(BufferedReader br = new BufferedReader(new FileReader(file.get()))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (current < start) {
          current += 1;
          continue;
        }
        
        if (current > end) {
          break;
        }
        
        lines.put(current, line);
        current += 1;
      }
    } catch (Exception e) {
      // If we get an IOException, not much we can do... just ignore it and move on.
      e.printStackTrace(System.err);
      return Optional.absent();
    }
    
    return Optional.of(lines.build());
  }
}
