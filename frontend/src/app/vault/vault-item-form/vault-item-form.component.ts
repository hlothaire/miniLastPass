import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { VaultItem } from '../vault.service';

@Component({
  selector: 'app-vault-item-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vault-item-form.component.html',
  styleUrl: './vault-item-form.component.scss'
})
export class VaultItemFormComponent implements OnChanges {
  @Input() item: VaultItem | null = null;
  @Output() saved = new EventEmitter<{ title: string; username: string; url?: string; secret?: string }>();
  @Output() cancelled = new EventEmitter<void>();

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    username: ['', Validators.required],
    url: [''],
    secret: ['', Validators.required]
  });

  constructor(private readonly fb: FormBuilder) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['item']) {
      if (this.item) {
        this.form.patchValue({
          title: this.item.title,
          username: this.item.username,
          url: this.item.url ?? '',
          secret: ''
        });
        this.form.controls.secret.clearValidators();
        this.form.controls.secret.updateValueAndValidity();
      } else {
        this.form.reset();
        this.form.controls.secret.setValidators([Validators.required]);
        this.form.controls.secret.updateValueAndValidity();
      }
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const payload = {
      title: value.title,
      username: value.username,
      url: value.url || undefined,
      secret: value.secret || undefined
    };

    this.saved.emit(payload);

    if (!this.item) {
      this.form.reset();
    }
  }

  cancel(): void {
    this.cancelled.emit();
  }
}
