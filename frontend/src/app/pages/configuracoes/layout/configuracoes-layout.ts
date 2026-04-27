import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-configuracoes-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './configuracoes-layout.html',
  styleUrls: ['./configuracoes-layout.scss']
})
export class ConfiguracoesLayoutComponent {}
