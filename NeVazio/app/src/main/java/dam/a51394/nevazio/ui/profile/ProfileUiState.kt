package dam.a51394.nevazio.ui.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val familyCode: String = "",
    val isLoading: Boolean = true,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showChangeNameDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showJoinFamilyDialog: Boolean = false,
    val showLeaveFamilyDialog: Boolean = false,
    val newNameInput: String = "",
    val newPasswordInput: String = "",
    val newFamilyCodeInput: String = ""
)
