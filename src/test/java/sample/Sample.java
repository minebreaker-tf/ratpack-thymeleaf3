package sample;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import ratpack.func.Function;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.embed.EphemeralBaseDir;
import ratpack.thymeleaf3.Template3;
import ratpack.thymeleaf3.Thymeleaf3Module;

import static com.google.common.truth.Truth.assertThat;

public final class Sample {

    @Test
    public void test() throws Exception {

        Thymeleaf3Module thymeleafModule = new Thymeleaf3Module();
        Function<Registry, Registry> registry = ratpack.guice.Guice.registry(
                spec -> spec.module( thymeleafModule ) );

        EphemeralBaseDir.tmpDir().use( baseDir -> {
            baseDir.write( "thymeleaf/template.html", "<p th:text=\"'hello, ' + ${name}\"/>" );

            EmbeddedApp.fromServer(
                    RatpackServer.of( server -> server
                            .serverConfig( ServerConfig.builder()
                                                       .development( true )
                                                       .baseDir( baseDir.getRoot() ) )
                            .registry( registry )
                            .handler( r -> ctx -> {
                                ctx.render( Template3.thymeleafTemplate( "template", ImmutableMap.of( "name", "world" ) ) );
                            } ) ) )
                       .test( client -> {
                           String result = client.get().getBody().getText();
                           assertThat( result ).isEqualTo( "<p>hello, world</p>" );
                       } );
        } );
    }

}
