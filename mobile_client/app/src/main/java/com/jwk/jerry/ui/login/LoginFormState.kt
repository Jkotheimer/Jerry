package com.jwk.jerry.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: String?,
    val passwordError: String?
) {
    val isDataValid: Boolean
        get() = usernameError == null && passwordError == null
}
