package eu.europa.fisheries.uvms.asset.service.arquillian;

import java.io.File;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

@ArquillianSuiteDeployment
public abstract class BuildAssetServiceDeployment {

    @Deployment(name = "normal", order = 1)
    public static Archive<?> createDeployment() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

//        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
//                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
//        testWar.addAsLibraries(files);

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.asset:asset-dbaccess-domain",
                         "eu.europa.ec.fisheries.uvms.asset:deprecated-asset-message",
                         "eu.europa.ec.fisheries.uvms.asset:deprecated-asset-message-mock",
                         "eu.europa.ec.fisheries.uvms.audit:audit-model",
                         "eu.europa.ec.fisheries.uvms:uvms-config",
                         "eu.europa.ec.fisheries.uvms.config:config-model:4.0.0")
                .withoutTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addPackages(true, "eu.europa.fisheries.uvms.asset.service");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.asset.service");

        return testWar;
    }
}