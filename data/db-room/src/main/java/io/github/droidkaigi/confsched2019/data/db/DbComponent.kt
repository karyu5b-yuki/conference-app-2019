package io.github.droidkaigi.confsched2019.data.db

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@Component(
    modules = [
        DbModule::class
    ]
)
interface DbComponent {
    fun sessionDatabase(): SessionDatabase
    fun sponsorDatabase(): SponsorDatabase
    fun announcementDatabase(): AnnouncementDatabase
    fun staffDatabase(): StaffDatabase
    fun contributorDatabase(): ContributorDatabase

    @Component.Builder
    interface Builder {
        @BindsInstance fun context(context: Context): Builder

        @BindsInstance fun coroutineContext(coroutineContext: CoroutineContext): Builder

        @BindsInstance fun filename(filename: String?): Builder

        fun build(): DbComponent
    }

    companion object {
        fun builder(): Builder = DaggerDbComponent.builder()
    }
}
