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
  selector: 'app-login',
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
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  error = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    if (this.authService.currentUserValue) {
      this.router.navigate(['/dashboard']);
    }
    this.loginForm = this.formBuilder.group({
      email: ['admin@gst.com', Validators.required],
      password: ['password123', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    
    this.authService.login(this.loginForm.value)
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: error => {
          this.error = 'Invalid credentials';
          this.loading = false;
          console.log(error);
          console.log(error.error);

         this.error = JSON.stringify(error.error);
         
 
        }
      });
  }
}
