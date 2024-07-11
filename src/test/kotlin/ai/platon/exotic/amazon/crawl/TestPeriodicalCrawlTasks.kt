package ai.platon.exotic.amazon.crawl

import ai.platon.pulsar.common.collect.UrlFeederHelper
import ai.platon.pulsar.common.collect.ExternalUrlLoader
import ai.platon.pulsar.common.getLogger
import ai.platon.exotic.amazon.crawl.core.handlers.parse.WebDataExtractorInstaller
import ai.platon.exotic.common.jdbc.JdbcConfig
import ai.platon.pulsar.crawl.parse.ParseFilters
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired

class TestPeriodicalCrawlTasks: TestBase() {

    private val logger = getLogger(this::class)

    @Autowired
    private lateinit var urlLoader: ExternalUrlLoader

    @Autowired
    private lateinit var parseFilters: ParseFilters

    override var enableCrawlLoop = false

    private val urlFeederHelper get() = UrlFeederHelper(crawlLoop.urlFeeder)

    @Before
    override fun setup() {
        var jdbcConfigFactory = {
            JdbcConfig("com.mysql.cj.jdbc.Driver","jdbc:mysql://localhost:3306/exotic-amazon","root","123456")
        }
        WebDataExtractorInstaller(jdbcConfigFactory,extractorFactory).install(parseFilters)
        super.setup()
    }
}
