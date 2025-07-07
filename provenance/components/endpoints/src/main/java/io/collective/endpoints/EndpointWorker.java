package io.collective.endpoints;

import io.collective.articles.ArticleDataGateway;
import io.collective.restsupport.RestTemplate;
import io.collective.workflow.Worker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.collective.rss.RSS;
import io.collective.rss.Item;
import java.io.IOException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class EndpointWorker implements Worker<EndpointTask> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate template;
    private final ArticleDataGateway gateway;

    public EndpointWorker(RestTemplate template, ArticleDataGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    @NotNull
    @Override
    public String getName() {
        return "ready";
    }

    @Override
    public void execute(EndpointTask task) throws IOException {
        String response = template.get(task.getEndpoint(), task.getAccept());
        gateway.clear();

        // todo - map rss results to an article infos collection and save articles infos
        // to the article gateway
        XmlMapper xmlMapper = new XmlMapper();
        RSS rss = xmlMapper.readValue(response, RSS.class);
        if (rss != null && rss.getChannel() != null && rss.getChannel().getItem() != null) {
            for (Item item : rss.getChannel().getItem()) {
                if (item.getTitle() != null) {
                    gateway.save(item.getTitle());
                }
            }
        }
    }
}
