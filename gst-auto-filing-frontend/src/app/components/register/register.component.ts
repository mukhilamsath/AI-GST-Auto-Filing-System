import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;
  error = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    if (this.authService.currentUserValue) {
      this.router.navigate(['/login']);
    }
    this.registerForm = this.formBuilder.group({
      email: ['admin@gst.com', Validators.required],
      password: ['password123', Validators.required]
    });
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    
    this.authService.login(this.registerForm.value)
      .subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: error => {
          this.error = 'Invalid credentials';
          console.log(error);
         this.error = error.error?.message || 'Login failed';
          this.loading = false;
        }
      });
  }
}
