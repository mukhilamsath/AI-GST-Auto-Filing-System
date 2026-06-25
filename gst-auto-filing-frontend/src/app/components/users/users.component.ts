import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  displayedColumns: string[] = ['id', 'name', 'email', 'role', 'actions'];
  createUserForm!: FormGroup;
  loading = false;
  submitting = false;
  error = '';
  successMsg = '';
  availableRoles: string[] = ['USER', 'REVIEWER', 'ADMIN'];

  constructor(
    private apiService: ApiService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit() {
    this.fetchUsers();
    this.createUserForm = this.formBuilder.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['USER', Validators.required]
    });
  }

  fetchUsers() {
    this.loading = true;
    this.apiService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load users list';
        this.loading = false;
      }
    });
  }

  onRoleChange(userId: number, newRole: string) {
    this.apiService.updateUserRole(userId, newRole).subscribe({
      next: () => {
        this.successMsg = 'User role updated successfully';
        setTimeout(() => this.successMsg = '', 3000);
        this.fetchUsers();
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to update user role';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  onDeleteUser(userId: number) {
    if (confirm('Are you sure you want to delete this user?')) {
      this.apiService.deleteUser(userId).subscribe({
        next: () => {
          this.successMsg = 'User deleted successfully';
          setTimeout(() => this.successMsg = '', 3000);
          this.fetchUsers();
        },
        error: (err) => {
          console.error(err);
          this.error = 'Failed to delete user';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  onSubmit() {
    if (this.createUserForm.invalid) {
      return;
    }
    this.submitting = true;
    this.error = '';
    this.successMsg = '';

    this.apiService.createUser(this.createUserForm.value).subscribe({
      next: () => {
        this.successMsg = 'User created successfully';
        this.createUserForm.reset({ role: 'USER' });
        this.submitting = false;
        this.fetchUsers();
      },
      error: (err) => {
        console.error(err);
        this.error = err.error?.message || 'Failed to create user';
        this.submitting = false;
      }
    });
  }
}
