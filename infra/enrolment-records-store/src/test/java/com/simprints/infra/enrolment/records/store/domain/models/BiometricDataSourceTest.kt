import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import org.junit.Test

class BiometricDataSourceTest {

    @Test
    fun `should return SIMPRINTS when value is not SIMPRINTS`() {
        val result = BiometricDataSource.fromString("SIMPRINTS")
        assertThat(result).isEqualTo(BiometricDataSource.SIMPRINTS)
    }

    @Test
    fun `should return COMMCARE when value is COMMCARE`() {
        val result = BiometricDataSource.fromString("COMMCARE")
        assertThat(result).isEqualTo(BiometricDataSource.COMMCARE)
    }

    @Test
    fun `should return SIMPRINTS when value is unknown`() {
        val result = BiometricDataSource.fromString("UNKNOWN")
        assertThat(result).isEqualTo(BiometricDataSource.SIMPRINTS)
    }
}