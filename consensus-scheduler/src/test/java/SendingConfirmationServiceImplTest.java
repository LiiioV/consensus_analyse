import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import services.SenderService;
import services.SendingConfirmationService;
import services.SendingConfirmationServiceImpl;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SendingConfirmationServiceImplTest {

    static SendingConfirmationService confirmationService;


    public SendingConfirmationServiceImplTest() {

    }

    @BeforeAll
    static void init() {
        confirmationService = new SendingConfirmationServiceImpl();
    }

    @Test
    public void sendCustomIdTest() {
        boolean consensusReached = confirmationService.sendForConfirmationCustomId(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json",
                0);

        assertTrue(consensusReached);
    }

    @Test
    public void sendCustomValuesTest() {
        boolean consensusReached = confirmationService.sendForConfirmationCustomValues(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json",
                1.0,
                23.99982489928296);

        assertTrue(consensusReached);
    }

    @Test
    public void sendMinMessagesTest() {
        boolean consensusReached = confirmationService.sendForConfirmationMinMessages(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json");

        assertTrue(consensusReached);
    }

    @Test
    public void sendMaxProbabilityTest() {
        boolean consensusReached = confirmationService.sendForConfirmationMaxProbability(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json");

        assertTrue(consensusReached);
    }

    static class SenderServiceImpl implements SenderService {
        private static final String PROPERTIES_PATH = "../prog-autom-consensus-analyzer/resources/cnf-1/cnf_1_properties.yaml";
        private HashMap<String, Double> orgReplyProbability;

        public SenderServiceImpl() {
            this.orgReplyProbability = new HashMap<>();
            loadProbabilitiesFromFile(PROPERTIES_PATH);
        }
        
        private void loadProbabilitiesFromFile(String filePath) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
                Yaml yaml = new Yaml();
                Map<String, List<Map<String, Object>>> data = yaml.load(inputStream);
                List<Map<String, Object>> organizations = data.get("Organizations");

                for (Map<String, Object> org : organizations) {
                    String name = (String) org.get("Name");
                    Double probability = (Double) org.get("Pr");
                    this.orgReplyProbability.put(name, probability);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getReply(String orgName, int transactionId) {
            Double probability = orgReplyProbability.get(orgName);
            if (probability == null) {
                throw new IllegalArgumentException("Organization not found: " + orgName);
            }
            return ThreadLocalRandom.current().nextDouble() <= probability ? 1 : 0;
        }

        @Override
        public int getMaxRequestNum() {
            return 10;
        }

        @Override
        public int getMaxRequestTotalNum() {
            return 50;
        }

        @Override
        public long getTimeoutSec() {
            return 0;
        }

        @Override
        public long getWaitingTimeSec() {
            return 0;
        }

        @Override
        public String getLogPath() {
            return "src/test/resources/runTest.log";
        }
    }
}
