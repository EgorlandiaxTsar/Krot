import {AfterViewInit, Component, HostListener, inject, signal} from '@angular/core'
import {ZardProgressBarComponent} from "@/shared/components/progress-bar"
import {AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms"
import {
  ZardFormControlComponent,
  ZardFormFieldComponent,
  ZardFormLabelComponent
} from "@/shared/components/form"
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
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class Auth implements AfterViewInit {
  private static readonly BACKEND_ADDRESS_FORM_ERRORS: Record<string, string> = {
    ArgumentError: 'Detected invalid arguments. This generally shouldn\'t happen, restart the app and try again.',
    ClientHelloFailed: 'Server isn\'t responding to \'/hello\' packet. Enter a valid Krot Backend server.',
    KeystoreAccessFailed: 'Failed to access secured OS keystore. Make sure to grant needed permissions to access it. Note: Krot Terminal requires access only to it\'s personal keystore data.',
    ClientCredentialsUpdateFailed: 'Failed to update internal HTTP client with new server address. This generally shouldn\'t happen, you can do the following things to fix this: clear Krot Terminal OS keystore data, clear app\'s cache, restart the app and try again.',
  }
  private static readonly LOGIN_FORM_ERRORS: Record<string, string> = {
    KeystoreAccessFailed: 'Failed to access secured OS keystore. Make sure to grant needed permissions to access it. Note: Krot Terminal requires access only to it\'s personal keystore data.',
    ClientCredentialsUpdateFailed: 'Failed to update internal HTTP client with new credentials. This generally shouldn\'t happen, you can do the following things to fix this: clear Krot Terminal OS keystore data, clear app\'s cache, restart the app and try again.',
    ClientAuthenticationFailed: 'Provided credentials are invalid. Make sure to enter correct login and password.'
  }

  authProgress = signal(0)
  authErrorMessage = signal<string | null>(null)
  authIsSubmitting = signal(false)

  backendAddressForm = new FormGroup({
    schema: new FormControl('http', [Validators.required]),
    ip: new FormControl('', [
      Validators.required,
      Validators.pattern(/^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/)
    ]),
    port: new FormControl('', [Validators.required, Validators.pattern(/^[0-9]{1,5}$/)])
  })
  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(32)]),
    password: new FormControl('', [Validators.required, Validators.minLength(8), Validators.maxLength(32)])
  })

  private router = inject(Router)

  get backendAddressFormSchema() {
    return this.backendAddressForm.get('schema')!
  }

  get backendAddressFormIp() {
    return this.backendAddressForm.get('ip')!
  }

  get backendAddressFormPort() {
    return this.backendAddressForm.get('port')!
  }

  get loginUsername() {
    return this.loginForm.get('username')!
  }

  get loginPassword() {
    return this.loginForm.get('password')!
  }

  async ngAfterViewInit(): Promise<void> {
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
    try {
      const server = await invoke<{ ip: string, port: number, secured: boolean }>('get_server_address')
      this.backendAddressForm.patchValue({
        schema: server.secured ? 'https' : 'http',
        ip: server.ip,
        port: server.port.toString()
      })
    } catch (error: any) {}
  }

  fieldState(field: AbstractControl) {
    return field.invalid && field.touched ? 'error' : undefined
  }

  async onBackendSubmit(): Promise<void> {
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
      this.authErrorMessage.set(Auth.BACKEND_ADDRESS_FORM_ERRORS[error.type] ?? 'Unknown Error!')
    } finally {
      this.authIsSubmitting.set(false)
    }
  }

  async onLoginSubmit(): Promise<void> {
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
      this.authErrorMessage.set(Auth.LOGIN_FORM_ERRORS[error.type] ?? 'Unknown Error!')
    } finally {
      this.authIsSubmitting.set(false)
    }
  }

  onBackRequest() {
    this.loginForm.reset()
    this.authProgress.set(0)
  }

  @HostListener('window:keydown', ['$event'])
  handleShortcuts(event: KeyboardEvent) {

  }
}
