import { n as e } from "./chunk-DiqZc92J.js";
import { Q as t, et as n, n as r, o as i, r as a, t as o, u as s } from "./icons-Daxdm7Mp.js";
import { a as c, d as l, i as u, l as d, n as f, o as p, r as m, s as h, t as g } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as _, r as v } from "./copilot-ui-state-DjlBBpyf.js";
import { r as y, t as b } from "./stats-D_j8yo6z.js";
import { n as x, t as S } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as C, t as w } from "./early-project-state-D-4_8bD-.js";
import { a as T, i as E, s as D, t as O } from "./copilot-development-setup-user-guide-utils-BxGBMF0F.js";
//#region frontend/copilot/copilot-devtools/copilot-devtools.ts
var k, A, j, M, N;
//#endregion
e((() => {
	h(), a(), u(), v(), r(), D(), n(), g(), b(), C(), S(), p(), k = "bg-[linear-gradient(to_right,var(--amber-3),var(--amber-5),var(--amber-3),var(--amber-6))] dark:bg-[linear-gradient(to_right,var(--amber-5),var(--amber-7),var(--amber-5),var(--amber-8))]", A = "bg-[linear-gradient(to_right,var(--blue-3),var(--blue-5),var(--blue-3),var(--blue-6))] dark:bg-[linear-gradient(to_right,var(--blue-4),var(--blue-6),var(--blue-4),var(--blue-7))]", j = "bg-[linear-gradient(to_right,var(--ruby-3),var(--ruby-5),var(--ruby-3),var(--ruby-6))] dark:bg-[linear-gradient(to_right,var(--ruby-4),var(--ruby-6),var(--ruby-4),var(--ruby-7))]", M = "bg-[linear-gradient(to_right,var(--teal-3),var(--teal-5),var(--teal-3),var(--teal-6))] dark:bg-[linear-gradient(to_right,var(--teal-4),var(--teal-6),var(--teal-4),var(--teal-7))]", N = class extends m {
		constructor(...e) {
			super(...e), this._helpExpanded = !1;
		}
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("flex", "flex-col");
		}
		render() {
			return s`
      <header class="flex items-center pe-2 ps-4 py-2">
        <h2 class="font-bold gap-1 me-auto my-0 text-xs uppercase">Vaadin Copilot</h2>
        <vaadin-button
          aria-label="Close"
          theme="icon tertiary"
          @click=${() => {
				this.closePopover();
			}}>
          <vaadin-icon .svg="${o.close}"></vaadin-icon>
          <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
        </vaadin-button>
      </header>
      <div class="flex flex-col gap-4 pb-4 px-4">
        ${this.renderUserButton()} ${this.renderDevelopmentWorkflow()} ${this.renderWelcomeToVersion()}
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleAppInfoClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.info}"></vaadin-icon>
            App Info
          </vaadin-button>
          <vaadin-button @click="${this.handleAppLogClick}" class="border-0 h-auto justify-start py-2" theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.terminal}"></vaadin-icon>
            App Log
          </vaadin-button>
          <vaadin-button
            @click="${this.handleFeaturesClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.listAlt}"></vaadin-icon>
            Features
          </vaadin-button>
          ${w.springSecurityEnabled ? s`
                <vaadin-button
                  @click="${this.handleImpersonateAppUserClick}"
                  class="border-0 h-auto justify-start py-2"
                  theme="tertiary">
                  <vaadin-icon slot="prefix" .svg="${o.accountCircle}"></vaadin-icon>
                  Impersonate App User
                </vaadin-button>
              ` : i}
        </div>
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleFeedbackClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.feedback}"></vaadin-icon>
            Feedback
          </vaadin-button>
          <vaadin-button
            @click="${this.toggleHelpAndSupport}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.help}"></vaadin-icon>
            Help & Support
            <vaadin-icon
              slot="suffix"
              .svg="${this._helpExpanded ? o.keyboardArrowUp : o.keyboardArrowDown}"></vaadin-icon>
          </vaadin-button>
          ${this._helpExpanded ? this.renderHelpLinks() : i}
          <vaadin-button
            @click="${this.handleSettingsClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${o.settings}"></vaadin-icon>
            Settings
          </vaadin-button>
        </div>
      </div>
    `;
		}
		renderUserButton() {
			let e = _.userInfo?.validLicense, t = e ? k : A, n = e ? "text-amber-12 dark:text-amber-11" : "text-blue-12 dark:text-blue-11", r = this.getUserName() !== "Log in";
			return s`
      <vaadin-button
        @click=${this.handleUserLoginClick}
        class="animate-gradient ${t} border-0 h-auto justify-start py-2 text-start ${r ? "gap-3 px-3" : "items-start"}">
        ${r ? this.renderUserImage() : s`<vaadin-icon
              class="text-blue-12 dark:text-blue-11"
              slot="prefix"
              .svg="${o.login}"></vaadin-icon>`}
        <span class="flex flex-col">
          <span>${this.getUserName()}</span>
          <span class="${n} text-xs">${this.getLicenseType()}</span>
        </span>
      </vaadin-button>
    `;
		}
		renderWelcomeToVersion() {
			let e = _.projectVersionReleaseNoteInfo;
			return e === null || x.getMostRecentReleaseNoteDismissed() || !e.mostRecentVersion || !e.url ? i : s`
      <div class="flex relative">
      <vaadin-button
        id="release-note-btn"
        data-test-id="release-note-btn"
        class="border-0 h-auto items-start justify-start px-3 py-2 text-start w-full"
        @click="${(t) => {
				window.open(e.url, "_blank");
			}}">
        <vaadin-icon class="text-blue-11" slot="prefix" .svg="${o.info}"></vaadin-icon>
        <span class="flex flex-col">
          <span>Welcome to Vaadin ${e.vaadinVersion}</span>
          <span class="text-blue-11 text-xs">Click for release notes</span>
        </span>
      </vaadin-button>
      <vaadin-button
        class="absolute end-0 top-0"
        id="dismiss-release-note-item"
        theme="icon tertiary"
        @click="${(e) => {
				e.stopPropagation(), x.setMostRecentReleaseNoteDismissed(!0);
			}}"
        ><vaadin-icon .svg="${o.close}"></vaadin-icon
        <vaadin-tooltip slot="tooltip" text="Dismiss"></vaadin-tooltip>
      </vaadin-button>
      </div>
    `;
		}
		renderUserImage() {
			return _.userInfo?.portraitUrl ? s`<img
        alt="${this.getUserName()}"
        class="rounded-full size-8 object-cover"
        slot="prefix"
        src="https://vaadin.com${_.userInfo.portraitUrl}" />` : i;
		}
		renderDevelopmentWorkflow() {
			let e = E(), t = T(), n = this.getDevelopmentWorkflowConfig(e, t), r = n?.bgClass ?? "", i = n?.colorClass ?? "", a = this.resolveIcon(n), o = n?.rotateIcon ? `rotate-180 ${i}` : i, c = this.resolveTitle(n), l = n?.displayMessage ?? "";
			return s`
      <vaadin-button
        data-test-id="development-workflow-btn"
        @click="${this.handleDevelopmentWorkflowClick}"
        class="animation-delay-4000 animate-gradient ${r} border-0 h-auto items-start justify-start py-2 text-start">
        <vaadin-icon class="${o}" slot="prefix" .svg="${a}"></vaadin-icon>
        <span class="flex flex-col">
          <span>${c}</span>
          <span class="text-xs ${i}">${l}</span>
        </span>
      </vaadin-button>
    `;
		}
		getDevelopmentWorkflowConfig(e, t) {
			let n = {
				bgClass: M,
				colorClass: "text-teal-11"
			};
			if (e === "warning" && t === "warning") return {
				...n,
				icon: o.wbIncandescent,
				rotateIcon: !0,
				title: "IDE plugin & Hotswap recommended",
				combinedTitle: !0,
				displayMessage: "Enable both for optimal development workflow"
			};
			if (e === "warning") return {
				...n,
				icon: o.wbIncandescent,
				rotateIcon: !0,
				title: "Hotswap recommended",
				displayMessage: "Applies changes without restarting"
			};
			if (t === "warning") return {
				...n,
				icon: o.code,
				getIcon: !0,
				title: "IDE plugin recommended",
				getTitle: !0,
				displayMessage: "Simplifies Hotswap setup & config"
			};
			if (e === "error") return {
				bgClass: j,
				colorClass: "text-ruby-11",
				icon: o.error,
				title: "Hotswap partially enabled",
				displayMessage: "View details"
			};
		}
		resolveIcon(e) {
			return e ? e.getIcon ? this.getIdeIcon() : e.icon : o.bolt;
		}
		resolveTitle(e) {
			return e ? e.combinedTitle ? this.getCombinedTitle() : e.getTitle ? this.getIdePluginName() : e.title : "Development Workflow";
		}
		getUserName() {
			return [_.userInfo?.firstName, _.userInfo?.lastName].filter(Boolean).join(" ") || "Log in";
		}
		getLicenseType() {
			return _.userInfo?.validLicense ? "" : "Unlock all Copilot features, including AI";
		}
		getIdeIcon() {
			switch (_.idePluginState?.ide) {
				case "intellij": return o.intelliJ;
				case "vscode": return o.vsCode;
				case "eclipse": return o.eclipse;
				default: return o.code;
			}
		}
		getIdePluginName() {
			switch (_.idePluginState?.ide) {
				case "intellij": return "Vaadin plugin for IntelliJ";
				case "vscode": return "Vaadin extension for VS Code";
				case "eclipse": return "Vaadin plugin for Eclipse";
				default: return "IDE plugin";
			}
		}
		getCombinedTitle() {
			switch (_.idePluginState?.ide) {
				case "intellij": return "IntelliJ plugin & Hotswap recommended";
				case "vscode": return "VS Code extension & Hotswap recommended";
				case "eclipse": return "Eclipse plugin & Hotswap recommended";
				default: return "IDE plugin & Hotswap recommended";
			}
		}
		closePopover() {
			let e = this.closest("vaadin-popover");
			e && (e.opened = !1);
		}
		handleUserLoginClick() {
			if (_.userInfo?.validLicense) {
				window.open("https://vaadin.com/myaccount", "_blank", "noopener");
				return;
			}
			_.setLoginCheckActive(!0);
		}
		handleDevelopmentWorkflowClick() {
			y("use-dev-workflow-guide"), f.openPanel(O), this.closePopover();
		}
		handleAppInfoClick() {
			f.openPanel(t.INFO), this.closePopover();
		}
		handleAppLogClick() {
			f.openPanel(t.LOG), this.closePopover();
		}
		handleFeaturesClick() {
			f.openPanel(t.FEATURES), this.closePopover();
		}
		handleImpersonateAppUserClick() {
			f.openPanel(t.IMPERSONATOR), this.closePopover();
		}
		handleSettingsClick() {
			f.openPanel(t.SETTINGS), this.closePopover();
		}
		handleFeedbackClick() {
			f.openPanel(t.FEEDBACK), this.closePopover();
		}
		toggleHelpAndSupport() {
			this._helpExpanded = !this._helpExpanded;
		}
		renderHelpLinks() {
			return s`
      <div class="flex flex-col ps-4">
        ${[
				{
					label: "Forum",
					icon: "forum",
					url: "https://vaadin.com/forum"
				},
				{
					label: "Docs",
					icon: "article",
					url: "https://vaadin.com/docs/latest/tools/copilot"
				},
				{
					label: "GitHub Issues",
					icon: "github",
					url: "https://github.com/vaadin/copilot/issues"
				}
			].map(({ label: e, icon: t, url: n }) => s`
            <vaadin-button
              @click="${() => window.open(n, "_blank", "noopener")}"
              class="border-0 h-auto justify-start py-2"
              theme="tertiary">
              <vaadin-icon slot="prefix" .svg="${o[t]}"></vaadin-icon>
              ${e}
            </vaadin-button>
          `)}
      </div>
    `;
		}
	}, c([d()], N.prototype, "_helpExpanded", void 0), N = c([l("copilot-devtools")], N);
}))();
