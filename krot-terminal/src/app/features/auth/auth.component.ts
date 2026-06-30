import {AfterViewInit, Component, inject, signal} from '@angular/core'
import {ZardProgressBarComponent} from "@/shared/components/progress-bar"
import {AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms"
import {ZardFormControlComponent, ZardFormFieldComponent, ZardFormLabelComponent} from "@/shared/components/form"
import {ZardInputDirective} from "@/shared/components/input"
import {ZardCardComponent} from "@/shared/components/card"
import {ZardButtonComponent} from "@/shared/components/button"
import {ZardSelectComponent, ZardSelectItemComponent} from "@/shared/components/select"
import {invoke} from "@tauri-apps/api/core"
import {ZardAlertComponent} from "@/shared/components/alert"
import {NgIcon, provideIcons} from "@ng-icons/core"
import {lucideArrowLeft} from "@ng-icons/lucide"
import {ZardKbdComponent} from "@/shared/components/kbd";
import {Router} from "@angular/router";
import {BackendAddress} from "@/core/models/common";

@Component({
  selector: 'app-auth',
  imports: [
    ZardProgressBarComponent,
    ReactiveFormsModule,
    ZardFormFieldComponent,
    ZardFormLabelComponent,
    ZardFormControlComponent,
    ZardInputDirective,
    ZardCardComponent,
    ZardButtonComponent,
    ZardSelectComponent,
    ZardSelectItemComponent,
    ZardAlertComponent,
    NgIcon,
    ZardKbdComponent
  ],
  viewProviders: [provideIcons({ lucideArrowLeft })],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css',
})
export class AuthComponent implements AfterViewInit {
  private static readonly BACKEND_ADDRESS_FORM_ERRORS: Record<string, string> = {
    ArgumentError: 'Detected invalid arguments. This generally shouldn\'t happen, restart the app and try again.',
    ClientHelloFailed: 'Server isn\'t responding to \'/hello\' packet. Enter a valid Krot Backend server.',
    KeystoreAccessFailed: 'Failed to access secured OS keystore. Make sure to grant needed permissions to access it. Note: Krot TerminalComponent requires access only to it\'s personal keystore data.',
    ClientCredentialsUpdateFailed: 'Failed to update internal HTTP client with new server address. This generally shouldn\'t happen, you can do the following things to fix this: clear Krot TerminalComponent OS keystore data, clear app\'s cache, restart the app and try again.',
  }
  private static readonly LOGIN_FORM_ERRORS: Record<string, string> = {
    KeystoreAccessFailed: 'Failed to access secured OS keystore. Make sure to grant needed permissions to access it. Note: Krot TerminalComponent requires access only to it\'s personal keystore data.',
    ClientCredentialsUpdateFailed: 'Failed to update internal HTTP client with new credentials. This generally shouldn\'t happen, you can do the following things to fix this: clear Krot TerminalComponent OS keystore data, clear app\'s cache, restart the app and try again.',
    ClientAuthenticationFailed: 'Provided credentials are invalid. Make sure to enter correct login and password.'
  }

  protected authProgress = signal(0)
  protected authErrorMessage = signal<string | null>(null)
  protected authIsSubmitting = signal(false)

  protected backendAddressForm = new FormGroup({
    schema: new FormControl('http', [Validators.required]),
    ip: new FormControl('', [
      Validators.required,
      Validators.pattern(/^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/)
    ]),
    port: new FormControl('', [Validators.required, Validators.pattern(/^[0-9]{1,5}$/)])
  })
  protected loginForm = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(32)]),
    password: new FormControl('', [Validators.required, Validators.minLength(8), Validators.maxLength(32)])
  })

  private router = inject(Router)

  protected get backendAddressFormSchema() {
    return this.backendAddressForm.get('schema')!
  }

  protected get backendAddressFormIp() {
    return this.backendAddressForm.get('ip')!
  }

  protected get backendAddressFormPort() {
    return this.backendAddressForm.get('port')!
  }

  protected get loginUsername() {
    return this.loginForm.get('username')!
  }

  protected get loginPassword() {
    return this.loginForm.get('password')!
  }

  async ngAfterViewInit(): Promise<void> {
    await this.checkSession()
    await this.loadCachedBackendAddress()
  }

  protected fieldState(field: AbstractControl) {
    return field.invalid && field.touched ? 'error' : undefined
  }

  protected async onBackendSubmit() {
    if (this.backendAddressForm.invalid) return
    this.authErrorMessage.set(null)
    this.authIsSubmitting.set(true)
    const values = this.backendAddressForm.value
    try {
      await invoke('set_server_address', {
        args: {
          ip: values.ip!,
          port: Number(values.port!),
          secured: values.schema == 'https'
        }
      })
      this.authProgress.set(50)
    } catch (error: any) {
      if (!error.type) {
        this.authErrorMessage.set('Internal Error!')
        return
      }
      this.authErrorMessage.set(AuthComponent.BACKEND_ADDRESS_FORM_ERRORS[error.type] ?? 'Unknown Error!')
    } finally {
      this.authIsSubmitting.set(false)
    }
  }

  protected async onLoginSubmit(): Promise<void> {
    if (this.loginForm.invalid) return
    this.authErrorMessage.set(null)
    this.authIsSubmitting.set(true)
    const value = this.loginForm.value
    try {
      await invoke('set_user_credentials', {
        args: {
          username: value.username!,
          password: value.password!
        }
      })
      await this.router.navigate(['/terminal'])
    } catch (error: any) {
      if (!error.type) {
        this.authErrorMessage.set('Internal Error!')
        return
      }
      this.authErrorMessage.set(AuthComponent.LOGIN_FORM_ERRORS[error.type] ?? 'Unknown Error!')
    } finally {
      this.authIsSubmitting.set(false)
    }
  }

  protected onBackRequest() {
    this.loginForm.reset()
    this.authProgress.set(0)
  }

  private async checkSession() {
    let hasSession: boolean;
    try {
      hasSession = await invoke<boolean>('has_session')
    } catch (error: any) {
      hasSession = false;
    }
    if (hasSession) {
      await this.router.navigate(['/terminal'])
      return;
    }
  }

  private async loadCachedBackendAddress() {
    try {
      const server = await invoke<BackendAddress>('get_server_address')
      this.backendAddressForm.patchValue({
        schema: server.secured ? 'https' : 'http',
        ip: server.ip,
        port: server.port.toString()
      })
    } catch (error: any) {
      if (error.type != 'CredentialsNotFound') {
        console.error(`Failed to load cached server: ${error}`)
      }
    }
  }
}
