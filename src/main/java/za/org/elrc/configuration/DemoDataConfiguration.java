package za.org.elrc.configuration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Configuration
@PropertySources({ @PropertySource(value = "classpath:engine.properties", ignoreResourceNotFound = true) })
public class DemoDataConfiguration {

    @Autowired
    protected IdentityService identityService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected Environment environment;

    @PostConstruct
    public void init() {

        if (Boolean.valueOf(environment.getProperty("create.demo.users", "true"))) {
            initDemoGroups();
            initDemoUsers();
        }

        if (Boolean.valueOf(environment.getProperty("create.demo.definitions", "true"))) {
            initDemoProcessDefinitions();
        }

        if (Boolean.valueOf(environment.getProperty("create.demo.models", "true"))) {
            initDemoModelData();
        }
    }

    protected void initDemoGroups() {
        String[] assignmentGroups = new String[] { "AGGRIEVED_PARTY", "MANAGER", "SENIOR_MANAGER", "ELRC_ADMIN", "CMO", "COMMISSIONER", "PANELLIST"};
        String[] securityGroups = new String[] { "USER", "ADMIN" };

        for (String groupId : assignmentGroups) {
            createGroup(groupId, "assignment");
        }

        for (String groupId : securityGroups) {
            createGroup(groupId, "security-role");
        }
    }

    protected void createGroup(String groupId, String type) {
        if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
            Group newGroup = identityService.newGroup(groupId);
            newGroup.setName(groupId.substring(0, 1).toUpperCase() + groupId.substring(1));
            newGroup.setType(type);
            identityService.saveGroup(newGroup);
        }
    }

    protected void initDemoUsers() {
        createUser("party", "Party", "Party", "party", "party@elrc.org.za", null, Arrays.asList("AGGRIEVED_PARTY"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("manager", "Manager", "Manager", "manager", "manager@elrc.org.za", null, Arrays.asList("MANAGER"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("smanager", "Senior", "Manager", "smanager", "smanager@elrc.org.za", null, Arrays.asList("SENIOR_MANAGER"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("eadmin", "Elrc", "Admin", "eadmin", "eadmin@elrc.org.za", null, Arrays.asList("ELRC_ADMIN"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("cmo", "CMO", "CMO", "cmo", "cmo@elrc.org.za", null, Arrays.asList("CMO"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("commissioner", "Commissioner", "Commissioner", "commissioner", "commissioner@elrc.org.za", null, Arrays.asList("COMMISSIONER"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

        createUser("panellist", "Panellist", "Panellist", "panellist", "panellist@elrc.org.za", null, Arrays.asList("PANELLIST"),
                Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

    }

    protected void createUser(String userId, String firstName, String lastName, String password, String email,
            String imageResource, List<String> groups, List<String> userInfo) {

        if (identityService.createUserQuery().userId(userId).count() == 0) {

            // Following data can already be set by demo setup script

            User user = identityService.newUser(userId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setEmail(email);
            identityService.saveUser(user);

            if (groups != null) {
                for (String group : groups) {
                    identityService.createMembership(userId, group);
                }
            }
        }


        // user info
        if (userInfo != null) {
            for (int i = 0; i < userInfo.size(); i += 2) {
                identityService.setUserInfo(userId, userInfo.get(i), userInfo.get(i + 1));
            }
        }

    }

    protected void initDemoProcessDefinitions() {

        String deploymentName = "Demo processes";
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(deploymentName)
                .list();

        if (deploymentList == null || deploymentList.isEmpty()) {

            //@formatter:off
            repositoryService.createDeployment().name(deploymentName)
                    .addClasspathResource("dispute-referral-process.bpmn")
                    .deploy();
            //@formatter:on
        }
    }

    protected void initDemoModelData() {
        createModelData("Demo model", "This is a demo model", "org/activiti/rest/demo/model/test.model.json");
    }

    protected void createModelData(String name, String description, String jsonFile) {
        List<Model> modelList = repositoryService.createModelQuery().modelName("Demo model").list();

        if (modelList == null || modelList.isEmpty()) {

            Model model = repositoryService.newModel();
            model.setName(name);

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put("name", name);
            modelObjectNode.put("description", description);
            model.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(model);

            try {
                InputStream svgStream = this.getClass().getClassLoader()
                        .getResourceAsStream("org/activiti/rest/demo/model/test.svg");
                repositoryService.addModelEditorSourceExtra(model.getId(), IOUtils.toByteArray(svgStream));
            } catch (Exception e) {
                // LOGGER.warn("Failed to read SVG", e);
            }

            try {
                InputStream editorJsonStream = this.getClass().getClassLoader().getResourceAsStream(jsonFile);
                repositoryService.addModelEditorSource(model.getId(), IOUtils.toByteArray(editorJsonStream));
            } catch (Exception e) {
                // LOGGER.warn("Failed to read editor JSON", e);
            }
        }
    }

    protected String randomSentence(String[] words, int length) {
        Random random = new Random();
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            strb.append(words[random.nextInt(words.length)]);
            strb.append(" ");
        }
        return strb.toString().trim();
    }

}
