package motobeans.architecture.development.components

import android.app.Application
import com.finance.app.TestActivity
import com.finance.app.presenter.presenter.*
import com.finance.app.utility.GetOtherDropDownFromApi
import com.finance.app.utility.LeadAndLoanDetail
import com.finance.app.view.activity.*
import com.finance.app.view.adapters.recycler.adapter.TempRecyclerAdapter
import com.finance.app.view.adapters.recycler.holder.TempHolder
import com.finance.app.view.customViews.CustomPersonalInfoView
import com.finance.app.view.customViews.CustomZipAddressView
import com.finance.app.view.fragment.*
import com.finance.app.viewModel.AppDataViewModel
import com.finance.app.viewModel.LeadDataViewModel
import com.finance.app.viewModel.SyncDataViewModel
import com.finance.app.viewModel.TempViewModel
import com.optcrm.optreporting.AppModule
import com.optcrm.optreporting.app.workers.UtilWorkersTask
import dagger.Component
import motobeans.architecture.customAppComponents.activity.BaseAppCompatActivity
import motobeans.architecture.customAppComponents.jetpack.SuperWorker
import motobeans.architecture.development.modules.NetworkModule
import motobeans.architecture.development.modules.PrimitivesModule
import motobeans.architecture.development.modules.UtilityModule
import javax.inject.Singleton
import com.finance.app.presenter.presenter.Presenter as Presenter1

/**
 * Created by munishkumarthakur on 04/11/17.
 */
@Singleton
@Component(modules = arrayOf(
    AppModule::class, NetworkModule::class, UtilityModule::class, PrimitivesModule::class
))
interface ApplicationComponent {

    fun inject(app: Application)

    /**
     * Activities
     */
    fun inject(activity: TestActivity)
    fun inject(activity: BaseAppCompatActivity)
    fun inject(activity: DashboardActivity)
    fun inject(activity: SyncActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: LoanApplicationActivity)
    fun inject(activity: SplashScreen)
    fun inject(creationActivity: CreateLeadActivity)
    fun inject(activity: AllLeadActivity)
    fun inject(activity: LeadDetailActivity)
    fun inject(activity: UpdateCallActivity)

    /**
     * Fragment
     */
    fun inject(fragment: TestFragment)
    fun inject(fragment: PersonalInfoFragment)
    fun inject(fragment: NavMenuFragment)
    fun inject(fragment: LoanInfoFragment)
    fun inject(fragment: BankDetailFragment)
    fun inject(fragment: EmploymentInfoFragment)
    fun inject(fragment: AssetLiabilityFragment)
    fun inject(fragment: ReferenceFragment)
    fun inject(fragment: PersonalFormFragment)
    fun inject(fragment: PersonalMainFragment)
    fun inject(fragment: DocumentCheckListFragment)
    fun inject(fragment: PropertyFragment)
    fun inject(fragment: LeadsListingFragment)
    /**
     * Presenters
     */
    fun inject(presenter: LoanAppPostPresenter)
    fun inject(presenter: SendOTPPresenter)
    fun inject(presenter: BasePresenter)
    fun inject(presenter: VerifyOTPPresenter)
    fun inject(presenter: LoanAppGetPresenter)
    fun inject(presenter: CoApplicantsPresenter)
    fun inject(presenter: TestPresenter)
    fun inject(presenter: TempSyncPresenter)
    fun inject(presenter: LoginPresenter)
    fun inject(presenter: AddLeadPresenter)
    fun inject(presenter: TransactionCategoryPresenter)
    fun inject(presenter: AllMasterDropdownPresenter)
    fun inject(presenter: SourceChannelPartnerNamePresenter)
    fun inject(presenter: LoanProductPresenter)
    fun inject(presenter: PinCodeDetailPresenter)
    fun inject(presenter: DocumentUploadPresenter)
    fun inject(presenter: GetAllLeadsPresenter)
    fun inject(presenter: StateDropdownPresenter)
    fun inject(presenter: DistrictPresenter)
    fun inject(presenter: CityPresenter)

    /**
     * View Model
     */
    fun inject(viewModel: TempViewModel)

    fun inject(viewModel: SyncDataViewModel)
    fun inject(viewModel: LeadDataViewModel)
    fun inject(viewModel: AppDataViewModel)

    /**
     * Adapters
     */
    fun inject(adapter: TempRecyclerAdapter)

    /**
     * Holders
     */
    fun inject(other: TempHolder)

    /**
     * Others
     */
    fun inject(other: SuperWorker)
    fun inject(other: UtilWorkersTask)
    fun inject(other: LeadAndLoanDetail)
    fun inject(other: CustomPersonalInfoView)
    fun inject(other: CustomZipAddressView)
    fun inject(presenter: Presenter1)
    fun inject(other: GetOtherDropDownFromApi)
}