import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import org.junit.Test

class BiometricDataSourceTest {

    @Test
    fun `should return SIMPRINTS when value is not SIMPRINTS`() {
        val result = BiometricDataSource.fromString("SIMPRINTS", "")
        assertThat(result).isEqualTo(BiometricDataSource.Simprints)
    }

    @Test
    fun `should return COMMCARE when value is COMMCARE`() {
        val result = BiometricDataSource.fromString("COMMCARE", "caller")
        assertThat(result).isEqualTo(BiometricDataSource.CommCare("caller"))
    }

    @Test
    fun `should return SIMPRINTS when value is unknown`() {
        val result = BiometricDataSource.fromString("UNKNOWN", "caller")
        assertThat(result).isEqualTo(BiometricDataSource.Simprints)
    }

    @Test
    fun `should return correct permission name for data sources`() {
        assertThat(BiometricDataSource.Simprints.permissionName()).isNull()
        assertThat(BiometricDataSource.CommCare("caller").permissionName()).isNotEmpty()
    }
}
