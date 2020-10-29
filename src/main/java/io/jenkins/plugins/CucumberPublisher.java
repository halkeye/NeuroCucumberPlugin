package io.jenkins.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static jenkins.model.Jenkins.getInstanceOrNull;
import static org.apache.commons.lang.StringUtils.strip;


public class CucumberPublisher extends Notifier {
    private final String path;
    private final String webhook1;
    private final String webhook2;
    private final String webhook3;
    private final String webhook4;
    private final String webhook5;

    @DataBoundConstructor
    public CucumberPublisher(String path, List<String> webhooks, String webhook1, String webhook2, String webhook3, String webhook4, String webhook5) {
        this.path = path;
        this.webhook1 = webhook1;
        this.webhook2 = webhook2;
        this.webhook3 = webhook3;
        this.webhook4 = webhook4;
        this.webhook5 = webhook5;
    }

    public String getPath() {
        return path;
    }

    public String getWebhook1() {
        return webhook1;
    }

    public String getWebhook2() {
        return webhook2;
    }

    public String getWebhook3() {
        return webhook3;
    }

    public String getWebhook4() {
        return webhook4;
    }

    public String getWebhook5() {
        return webhook5;
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        listener.getLogger().println("Executing Cucumber plugin");
        try {
            build.addAction(new TestAction(build, path));
            Build buildTO = new Builder(Objects.requireNonNull(getInstanceOrNull()).getRootUrl(), build.getUrl())
                    .withBuildNumber(build.getNumber())
                    .withProject(build.getParent().getName())
                    .build();
            List<String> webhooks = fill(webhook1, webhook2, webhook3, webhook4, webhook5);
            webhooks.forEach(url -> {
                try {
                    HttpClient client = new HttpClient(url);
                    client.post("", buildTO);
                } catch (UnsupportedEncodingException | JsonProcessingException e) {
                    listener.getLogger().println(e.getMessage());
                }
            });
        } catch (IOException | InterruptedException e) {
            listener.getLogger().println(e.getMessage());
            return false;
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Neuro cucumber reporter";
        }

        public FormValidation doCheckPath(@QueryParameter String value) {
            return checkFieldNotEmpty(value);
        }

        private FormValidation checkFieldNotEmpty(String value) {
            value = strip(value);
            if (value == null || value.equals("")) {
                return FormValidation.error("This field must not be empty");
            }
            return FormValidation.ok();
        }
    }

    List<String> fill(String item1, String item2, String item3, String item4, String item5) {
        List<String> list = new ArrayList<>();
        if (item1 != null && !item1.trim().equals(""))
            list.add(item1);
        if (item2 != null && !item2.trim().equals(""))
            list.add(item2);
        if (item3 != null && !item3.trim().equals(""))
            list.add(item3);
        if (item4 != null && !item4.trim().equals(""))
            list.add(item4);
        if (item5 != null && !item5.trim().equals(""))
            list.add(item5);
        return list;
    }

}