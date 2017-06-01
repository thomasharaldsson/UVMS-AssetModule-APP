package eu.europa.fisheries.uvms.asset.service.arquillian;

import eu.europa.ec.fisheries.uvms.asset.service.bean.*;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ArquillianSuiteDeployment
public abstract class BuildAssetServiceDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildAssetServiceDeployment.class);


    @Deployment(name = "normal", order = 1)
    public static Archive<?> createDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        printFiles(files);

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
        testWar.addPackages(true, "com.tocea.easycoverage.framework.api");
        testWar.addPackages(true, "eu.europa.fisheries.uvms.asset.service");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.asset.service");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.asset.dto");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.asset.exception");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.asset.service.bean");


        testWar.addClass(TransactionalTests.class);
        testWar.addClass(ConfigServiceException.class);
        testWar.addClass(AssetHistoryServiceBean.class);
        testWar.addClass(ConfigServiceBean.class);
        testWar.addClass(AssetServiceBean.class);
        testWar.addClass(AssetEventServiceBean.class);
        testWar.addClass(AssetGroupServiceBean.class);


        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);



        return testWar;
    }

    private static void printFiles(File[] files) {

        List<File> filesSorted = new ArrayList<>();
        for(File f : files){
            filesSorted.add(f);
        }

        Collections.sort(filesSorted, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        LOG.info("FROM POM - begin");
        for(File f : filesSorted){
            LOG.info("       --->>>   "   +   f.getName());
        }
        LOG.info("FROM POM - end");
    }


}
