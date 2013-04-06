package facebook;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

public class Grabber {
    
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n\n\n\n");
        System.out.println("===============================================");
        System.out.println("Open your web browser on http://localhost:9090");
        System.out.println("Press ctrl+c to stop this example");
        System.out.println("===============================================");
        System.out.println("\n\n\n\n");

        // create a new Camel Main so we can easily start Camel
        final Main main = new Main();
        

        // enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
        main.enableHangupSupport();
        
        main.addRouteBuilder(new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                
                onException(Exception.class).handled(true);
                JsonDataFormat jsonFormat = new JsonDataFormat(JsonLibrary.Jackson);
                jsonFormat.setUnmarshalType(FacebookUser.class);
                
                from("direct:start")
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
                .to("http4:graph.facebook.com")
                .unmarshal(jsonFormat)
                .to("jpa:facebook.FacebookUser");
                

            }
        });

        ExecutorService executorService=Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(5000);
                    for (int idx= 4;idx<100000;idx++) {
                        main.getCamelTemplate().sendBodyAndHeader("direct:start","",Exchange.HTTP_PATH,"/"+idx);
                        Thread.sleep((int)(Math.random()*1000));
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CamelExecutionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                
            }
            
        });

        // and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
        main.run();

        
    }

}
