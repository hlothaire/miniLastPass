import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { VaultService } from '../vault.service';

@Component({
  selector: 'app-reveal-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reveal-button.component.html',
  styleUrl: './reveal-button.component.scss'
})
export class RevealButtonComponent {
  @Input() itemId!: string;
  revealed = false;
  secret: string | null = null;
  error: string | null = null;
  loading = false;

  constructor(private readonly vaultService: VaultService) {}

  toggleReveal(): void {
    if (this.revealed) {
      this.revealed = false;
      this.secret = null;
      return;
    }
    this.loading = true;
    this.vaultService.reveal(this.itemId).subscribe({
      next: result => {
        this.secret = result.secret;
        this.revealed = true;
        this.error = null;
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.error ?? 'Unable to reveal secret';
        this.loading = false;
      }
    });
  }
}
