package ai.platon.exotic.amazon.crawl.boot

import ai.platon.exotic.amazon.crawl.boot.component.AmazonCrawler
import ai.platon.exotic.amazon.crawl.boot.component.AmazonJdbcSinkSQLExtractor
import ai.platon.exotic.amazon.crawl.core.handlers.fetch.AmazonDetailPageHtmlChecker
import ai.platon.exotic.amazon.crawl.core.handlers.fetch.AmazonPageCategorySniffer
import ai.platon.exotic.amazon.crawl.core.handlers.parse.WebDataExtractorInstaller
import ai.platon.exotic.common.jdbc.JdbcConfig
import ai.platon.pulsar.common.getLogger
import ai.platon.pulsar.crawl.parse.ParseFilters
import ai.platon.pulsar.persist.HadoopUtils
import ai.platon.pulsar.protocol.browser.emulator.BrowserResponseHandler
import ai.platon.scent.ScentSession
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

data class JdbcCommitConfig(
    val name: String = "",
    var minNumNonBlankFields: Int = 1,
) {
    override fun toString(): String {
        return name
    }
}

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(
    "ai.platon.scent.boot.autoconfigure",
    "ai.platon.exotic.amazon.crawl.boot",
)
class CrawlerConfiguration(
    /**
     * The scent session
     * */
    private val session: ScentSession,
    /**
     * The amazon crawler which is the main entry for business code to crawl amazon.com
     * */
    private val amazonCrawler: AmazonCrawler,
    /**
     * The parse filter manager which is an extension point to add custom code to handle with HTML documents
     * */
    private val parseFilters: ParseFilters,
    /**
     * The browser's response handler
     * */
    private val responseHandler: BrowserResponseHandler,
    /**
     * Spring's ApplicationContext
     * */
    private val applicationContext: ApplicationContext,
    private val environment: Environment,
) {
    private val logger = getLogger(CrawlerConfiguration::javaClass)

    @Bean
    fun checkConfiguration() {
        val conf = session.unmodifiedConfig

        logger.info("{}", conf)
        val hadoopConf = HadoopUtils.toHadoopConfiguration(conf)
        logger.info("{}", hadoopConf)
    }

    /**
     * Initialize and start amazon crawler
     * */
    @Bean
    fun initializeCrawler() {
        val conf = session.sessionConfig
        responseHandler.htmlIntegrityChecker.addLast(AmazonDetailPageHtmlChecker(conf))
        responseHandler.pageCategorySniffer.addLast(AmazonPageCategorySniffer(conf))

        var jdbcConfigFactory = {
            JdbcConfig(environment.getProperty("spring.datasource.driver-class-name",""),environment.getProperty("spring.datasource.url",""),environment.getProperty("spring.datasource.username",""),environment.getProperty("spring.datasource.password",""))
        }

        val extractorFactory = { conf: JdbcCommitConfig ->
            applicationContext.getBean<AmazonJdbcSinkSQLExtractor>()
        }

        WebDataExtractorInstaller(jdbcConfigFactory,extractorFactory).install(parseFilters)
    }

}
