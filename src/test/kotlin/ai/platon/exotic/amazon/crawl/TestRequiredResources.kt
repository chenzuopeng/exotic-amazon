package ai.platon.exotic.amazon.crawl

import ai.platon.exotic.amazon.crawl.core.handlers.parse.WebDataExtractorInstaller
import ai.platon.exotic.common.jdbc.JdbcConfig
import ai.platon.pulsar.common.ResourceLoader
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertTrue

class TestRequiredResources: TestBase() {
    override var enableCrawlLoop = false

    @Ignore("ResourceLoader.exists not properly handle resources in target/classes directory")
    @Test
    fun `Ensure periodical tasks exist`() {
        // Failed to detect resource existence
        // java.lang.AssertionError: /home/vincent/workspace/exotic-amazon-main/target/classes/sites/amazon/crawl/generate/periodical
        amazonGenerator.searchPeriodicalSeedDirectories().map { it.toString().substringBeforeLast("/") }
            .forEach { dir ->
                println(dir)
                assertTrue(dir) { ResourceLoader.exists(dir) }
            }
    }

    @Test
    fun `Ensure crawl resources exist`() {
        val sqls = "sites/amazon/crawl/parse/sql"
        assertTrue { ResourceLoader.exists(sqls) }
        assertTrue { ResourceLoader.exists("$sqls/crawl/x-asin.sql") }

        var jdbcConfigFactory = {
            JdbcConfig("com.mysql.cj.jdbc.Driver","jdbc:mysql://localhost:3306/exotic-amazon","root","123456")
        }
        val extractorInstaller = WebDataExtractorInstaller(jdbcConfigFactory,extractorFactory)
//        assertTrue { ResourceLoader.exists(extractorInstaller.jdbcConfig) }
    }
}
