import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { VaultService, VaultItem, VaultItemPayload } from '../vault.service';
import { VaultItemFormComponent } from '../vault-item-form/vault-item-form.component';
import { RevealButtonComponent } from '../reveal-button/reveal-button.component';

@Component({
  selector: 'app-vault-list',
  standalone: true,
  imports: [CommonModule, RouterLink, VaultItemFormComponent, RevealButtonComponent],
  templateUrl: './vault-list.component.html',
  styleUrl: './vault-list.component.scss'
})
export class VaultListComponent implements OnInit {
  items: VaultItem[] = [];
  creating = false;
  editing: VaultItem | null = null;
  message: string | null = null;

  constructor(private readonly vaultService: VaultService) {}

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.message = null;
    this.vaultService.list().subscribe({
      next: items => (this.items = items),
      error: () => (this.message = 'Failed to load vault items')
    });
  }

  openCreate(): void {
    this.creating = true;
    this.editing = null;
  }

  startEdit(item: VaultItem): void {
    this.editing = item;
    this.creating = false;
  }

  cancel(): void {
    this.creating = false;
    this.editing = null;
  }

  saveCreate(payload: VaultItemPayload): void {
    this.vaultService.create(payload).subscribe({
      next: () => {
        this.creating = false;
        this.loadItems();
      },
      error: () => (this.message = 'Could not create item')
    });
  }

  saveEdit(payload: VaultItemPayload): void {
    if (!this.editing) {
      return;
    }
    this.vaultService.update(this.editing.id, payload).subscribe({
      next: () => {
        this.editing = null;
        this.loadItems();
      },
      error: () => (this.message = 'Could not update item')
    });
  }

  delete(item: VaultItem): void {
    this.vaultService.remove(item.id).subscribe({
      next: () => this.loadItems(),
      error: () => (this.message = 'Failed to delete item')
    });
  }
}
