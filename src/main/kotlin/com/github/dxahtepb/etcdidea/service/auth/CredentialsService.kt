package com.github.dxahtepb.etcdidea.service.auth

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.ServiceManager.getService

internal class CredentialsService {
    companion object {
        @JvmStatic
        val instance: CredentialsService
            get() = getService(CredentialsService::class.java)
    }

    fun getPassword(key: PasswordKey): String? {
        val credentials = PasswordSafe.instance.get(createCredentialAttributes(key.provideKey()))
        return credentials?.getPasswordAsString()
    }

    fun storePassword(key: PasswordKey, password: CharArray) {
        val credentials = Credentials(null, password)
        PasswordSafe.instance.set(createCredentialAttributes(key.provideKey()), credentials)
    }

    fun forgetPassword(key: PasswordKey) {
        PasswordSafe.instance.set(createCredentialAttributes(key.provideKey()), null)
    }
}

internal data class PasswordKey(val key: String) {
    fun provideKey() = key
}

private fun createCredentialAttributes(key: String): CredentialAttributes {
    return CredentialAttributes(generateServiceName("Etcd", key))
}
