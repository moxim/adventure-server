import { l as g, ai as x, $ as l, u as f, x as o, y as m, E as h, aj as t, q as y, e as $, ak as C, P as w, n as T } from "./copilot-Bm5k-h74.js";
import { r as p } from "./state-BH_9RcTJ.js";
import { i as s } from "./icons-mWh87_oB.js";
import { L as S } from "./lit-renderer-DHSDc4RC.js";
import { B as k } from "./base-panel-Cxtbncvf.js";
/**
 * @license
 * Copyright (c) 2017 - 2025 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */
class A extends S {
  /**
   * Adds the renderer callback to the select.
   */
  addRenderer() {
    this.element.renderer = (e, i) => {
      this.renderRenderer(e, i);
    };
  }
  /**
   * Runs the renderer callback on the select.
   */
  runRenderer() {
    this.element.requestContentUpdate();
  }
  /**
   * Removes the renderer callback from the select.
   */
  removeRenderer() {
    this.element.renderer = null;
  }
}
const P = g(A), u = window.Vaadin.copilot.tree;
if (!u)
  throw new Error("Tried to access copilot tree before it was initialized.");
var E = Object.defineProperty, R = Object.getOwnPropertyDescriptor, r = (a, e, i, c) => {
  for (var d = c > 1 ? void 0 : c ? R(e, i) : e, b = a.length - 1, v; b >= 0; b--)
    (v = a[b]) && (d = (c ? v(e, i, d) : v(d)) || d);
  return c && d && E(e, i, d), d;
};
let n = class extends k {
  constructor() {
    super(...arguments), this.selectedTab = 0, this.activationShortcutEnabled = l.isActivationShortcut(), this.aiUsage = l.isAIUsageAllowed(), this.aiProvider = l.getAIProvider(), this.hideCopilotRequestOngoing = !1, this.hideCopilotDialogVisible = !1, this.themeItems = [
      { label: "System", value: "system" },
      { label: "Light", value: "light" },
      { label: "Dark", value: "dark" }
    ], this.toolbarExpandModeItems = [
      {
        label: "Proximity",
        value: "proximity",
        description: "The toolbar expands and becomes fully visible as the mouse pointer approaches it."
      },
      {
        label: "Click",
        value: "click",
        description: "The toolbar expands and becomes fully visible when Play mode is clicked."
      },
      {
        label: "Hover",
        value: "hover",
        description: "The toolbar expands and becomes fully visible when the mouse hovers over it."
      },
      {
        label: "Always expanded",
        value: "always",
        description: "The toolbar remains fully visible at all times and never collapses or becomes translucent."
      },
      {
        label: "Disabled",
        value: "never",
        description: "Only Play mode is visible. Changing Copilot mode is not available, and keyboard shortcuts are disabled."
      }
    ], this.aiUsageItems = [
      { label: "Ask each time", value: "ask" },
      { label: "Allow", value: "yes" },
      { label: "Deny", value: "no" }
    ], this.aiProviderItems = [
      { label: "Any region", value: "ANY" },
      { label: "EU only", value: "EU_ONLY" }
    ], this.toggleActivationShortcut = () => {
      this.activationShortcutEnabled = !this.activationShortcutEnabled, l.setActivationShortcut(this.activationShortcutEnabled);
    };
  }
  connectedCallback() {
    super.connectedCallback(), this.style.display = "block", this.classList.add("h-full");
  }
  updated(a) {
    super.updated(a);
    const e = this.querySelector('[part="general-tab-container"]'), i = this.querySelector("vaadin-tabs");
    e && i && (e.style.height = `calc(100% - ${i.getBoundingClientRect().height}px)`);
  }
  renderKbd(a) {
    const e = a.replace(/<kbd([^>]*)class="([^"]*)"/, '<kbd$1class="$2 font-sans ms-auto"').replace(/<kbd(?![^>]*class=)/, '<kbd class="font-sans ms-auto"');
    return f(e);
  }
  render() {
    return o`
      <vaadin-tabs>
        <vaadin-tab ?selected=${this.selectedTab === 0} @click=${() => this.selectedTab = 0}>General</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 1} @click=${() => this.selectedTab = 1}>Shortcuts</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 2} @click=${() => this.selectedTab = 2}>AI</vaadin-tab>
      </vaadin-tabs>
      ${this.selectedTab === 0 ? this.renderGeneralTab() : null}
      ${this.selectedTab === 1 ? this.renderShortcutsTab() : null} ${this.selectedTab === 2 ? this.renderAiTab() : null}
    `;
  }
  renderGeneralTab() {
    const a = l.getSelectedTheme(), e = l.getToolbarExpandMode();
    return o`
      <div class="w-full flex flex-col justify-between" part="general-tab-container">
        <div class="border-dashed flex flex-col divide-y px-4 py-0.5">
          <div class="flex gap-2 items-start justify-between py-2">
            <label class="py-1.5" id="theme">Theme</label>
            <vaadin-select
              accessible-name-ref="theme"
              class="flex-shrink-0"
              theme="auto-width no-border"
              .items="${this.themeItems}"
              .value="${a}"
              @change="${(i) => {
      l.setSelectedTheme(
        i.target.value
      );
    }}"></vaadin-select>
          </div>
          <div class="flex gap-2 items-start justify-between py-2">
            <div class="flex flex-col py-1.5">
              <label id="toolbar-button-expand-mode">Toolbar behavior</label>
              <span class="text-secondary text-xs">How it appears & expands</span>
            </div>
            <vaadin-select
              accessible-name-ref="toolbar-expand-mode"
              class="flex-shrink-0"
              theme="auto-width no-border"
              .value="${e}"
              ${P(
      () => o`
                  <vaadin-list-box class="max-w-xs">
                    ${this.toolbarExpandModeItems.map(
        (i) => o`
                        <vaadin-item class="items-start" label="${i.label}" value="${i.value}">
                          <span class="flex flex-col gap-0.5">
                            <span>${i.label}</span>
                            <span class="text-secondary text-xs">${i.description}</span>
                          </span>
                        </vaadin-item>
                      `
      )}
                  </vaadin-list-box>
                `
    )}
              @change="${(i) => {
      const c = l.getToolbarExpandMode();
      l.setToolbarExpandMode(
        i.target.value
      ), m("toolbar-expand-mode-change", {
        selected: l.getToolbarExpandMode(),
        previous: c
      });
    }}"></vaadin-select>
          </div>
          <div class="flex gap-2 justify-between py-3.5">
            <div class="flex flex-col">
              <label id="reduce-motion-label">Reduce motion</label>
              <span id="reduce-motion-desc" class="text-secondary text-xs">Disables animations</span>
            </div>
            <button
              aria-checked="true"
              aria-labelledby="reduce-motion-label"
              aria-describedby="reduce-motion-desc"
              class="my-px"
              role="switch"
              type="button">
              <span></span>
            </button>
          </div>
          <div class="flex gap-2 justify-between py-3.5">
            <div class="flex flex-col">
              <label id="error-reports-label">Send error reports</label>
              <span id="error-reports-desc" class="text-secondary text-xs">Helps us improve the user experience</span>
            </div>
            <button
              aria-checked="true"
              aria-labelledby="error-reports-label"
              aria-describedby="error-reports-desc"
              class="my-px"
              role="switch"
              type="button">
              <span></span>
            </button>
          </div>
        </div>
        <div class="w-full px-4 py-0.5 mb-4" style="box-sizing: border-box">
          <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
            <vaadin-button
              data-test-id="hide-copilot-btn"
              @click="${this.handleHideCopilotButtonClick}"
              class="border-0 h-auto justify-start py-2"
              theme="tertiary">
              <vaadin-icon slot="prefix" .svg="${s.close}"></vaadin-icon>
              <span class="flex flex-col items-start">
                <span>Hide Copilot until server restart</span>
              </span>
            </vaadin-button>
          </div>
        </div>
      </div>

      <vaadin-confirm-dialog
        id="hideCopilotDialog"
        header="Temporarily Hide Copilot"
        .confirmText=${this.hideCopilotRequestOngoing ? "Hiding…" : "Continue"}
        cancel-text="Cancel"
        cancel-button-visible
        confirm-theme="primary"
        .confirmDisabled=${this.hideCopilotRequestOngoing}
        .cancelDisabled=${this.hideCopilotRequestOngoing}
        .noCloseOnEsc=${this.hideCopilotRequestOngoing}
        .opened="${this.hideCopilotDialogVisible}"
        .noCloseOnOutsideClick=${this.hideCopilotRequestOngoing}
        @cancel=${() => {
      this.hideCopilotDialogVisible = !1;
    }}
        @confirm=${this.onDisableConfirm}>
        This will hide the Copilot until the server restarts. The page will reload to apply the change. Do you want to
        continue?
        ${this.hideCopilotRequestOngoing ? o`
              <div style="display:flex; align-items:center; gap:var(--lumo-space-s); margin-top:var(--lumo-space-m);">
                <vaadin-progress-indicator indeterminate></vaadin-progress-indicator>
                <span>Hiding…</span>
              </div>
            ` : null}
      </vaadin-confirm-dialog>
    `;
  }
  renderShortcutsTab() {
    const a = u.hasFlowComponents();
    return o`<div class="flex flex-col gap-4 pb-2 pt-4 px-4 ">
      <div class="flex justify-between">
        <div class="flex flex-col">
          <label id="enable-shortcuts-label">Enable keyboard shortcut</label>
          <span id="enable-shortcuts-desc" class="text-secondary text-xs"
            >Switch anytime to Play mode with ${this.renderKbd(t.toggleCopilot)}</span
          >
        </div>
        <button
          aria-checked="${this.activationShortcutEnabled}"
          aria-labelledby="enable-shortcuts-label"
          aria-describedby="enable-shortcuts-desc"
          class="my-px"
          role="switch"
          type="button"
          @click=${() => this.toggleActivationShortcut()}>
          <span></span>
        </button>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Global</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.vaadin}"></vaadin-icon>
            <span>Switch Play Mode / Last Mode</span>
            ${this.renderKbd(t.toggleCopilot)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.undo}"></vaadin-icon>
            <span>Undo</span>
            ${this.renderKbd(t.undo)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.redo}"></vaadin-icon>
            <span>Redo</span>
            ${this.renderKbd(t.redo)}
          </li>
        </ul>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Component Selection</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.sparkles}"></vaadin-icon>
            <span>Open AI prompt</span>
            ${this.renderKbd(t.openAiPopover)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.code}"></vaadin-icon>
            <span>Go to source</span>
            ${this.renderKbd(t.goToSource)}
          </li>
          ${a ? o`<li class="flex gap-2 py-2">
                <vaadin-icon .svg="${s.code}"></vaadin-icon>
                <span>Go to attach source</span>
                ${this.renderKbd(t.goToAttachSource)}
              </li>` : h}
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.contentCopy}"></vaadin-icon>
            <span>Copy</span>
            ${this.renderKbd(t.copy)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.contentPaste}"></vaadin-icon>
            <span>Paste</span>
            ${this.renderKbd(t.paste)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.fileCopy}"></vaadin-icon>
            <span>Duplicate</span>
            ${this.renderKbd(t.duplicate)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.turnLeft}"></vaadin-icon>
            <span>Select parent</span>
            ${this.renderKbd(t.selectParent)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.north}"></vaadin-icon>
            <span>Select previous sibling</span>
            ${this.renderKbd(t.selectPreviousSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.south}"></vaadin-icon>
            <span>Select first child / next sibling</span>
            ${this.renderKbd(t.selectNextSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.delete}"></vaadin-icon>
            <span>Delete</span>
            ${this.renderKbd(t.delete)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.dashboardCustomize}"></vaadin-icon>
            <span>Add component</span>
            <kbd class="font-sans ms-auto">A – Z</kbd>
          </li>
        </ul>
      </div>
    </div>`;
  }
  renderAiTab() {
    const a = y.userInfo?.vaadiner;
    return o`<div class="border-dashed flex flex-col divide-y px-4 py-0.5">
      <div class="flex gap-2 items-start justify-between py-2">
        <div class="flex flex-col py-1.5">
          <label id="ai-usage">AI usage</label>
          <span class="text-secondary text-xs">All AI features are clearly labelled </span>
        </div>
        <vaadin-select
          accessible-name-ref="ai-usage"
          class="flex-shrink-0"
          theme="auto-width no-border"
          .items="${this.aiUsageItems}"
          .value="${this.aiUsage}"
          @value-changed="${(e) => {
      this.aiUsage = e.detail.value, l.setAIUsageAllowed(e.detail.value);
    }}"></vaadin-select>
      </div>
      ${a ? o`<div class="flex gap-2 items-start justify-between py-2">
            <label class="py-1.5" id="ai-provider">AI provider</label>
            <vaadin-select
              accessible-name-ref="ai-provider"
              class="flex-shrink-0"
              theme="auto-width no-border"
              .items="${this.aiProviderItems}"
              .value="${this.aiProvider}"
              @value-changed="${(e) => {
      this.aiProvider = e.detail.value, l.setAIProvider(e.detail.value);
    }}"></vaadin-select>
          </div>` : h}
    </div>`;
  }
  handleHideCopilotButtonClick() {
    this.hideCopilotDialogVisible = !0;
  }
  onDisableConfirm() {
    this.hideCopilotRequestOngoing = !0, $(`${w}hide-copilot`, {}, (a) => {
      C(a.data, {}) || (this.hideCopilotRequestOngoing = !1, window.location.reload());
    });
  }
};
r([
  p()
], n.prototype, "selectedTab", 2);
r([
  p()
], n.prototype, "activationShortcutEnabled", 2);
r([
  p()
], n.prototype, "aiUsage", 2);
r([
  p()
], n.prototype, "aiProvider", 2);
r([
  p()
], n.prototype, "hideCopilotRequestOngoing", 2);
r([
  p()
], n.prototype, "hideCopilotDialogVisible", 2);
n = r([
  T("copilot-settings-panel")
], n);
const D = {
  header: "Settings",
  tag: x.SETTINGS
}, I = {
  init(a) {
    a.addPanel(D);
  }
};
window.Vaadin.copilot.plugins.push(I);
export {
  n as CopilotSettingsPanel,
  D as panelConfig
};
