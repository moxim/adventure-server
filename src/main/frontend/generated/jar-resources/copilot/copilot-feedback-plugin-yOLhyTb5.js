import { x as c, q as f, c as d, p as v, y as h, s as y, P as m, a0 as g, n as k } from "./copilot-Bm5k-h74.js";
import { r as s } from "./state-BH_9RcTJ.js";
import { B as w } from "./base-panel-Cxtbncvf.js";
import { i as $ } from "./icons-mWh87_oB.js";
const x = "copilot-feedback-panel{display:flex;flex-direction:column;font:var(--copilot-font-xs);gap:var(--space-200);padding:var(--space-150)}copilot-feedback-panel>p{margin:0}copilot-feedback-panel .dialog-footer{display:flex;gap:var(--space-100)}copilot-feedback-panel :is(vaadin-select,vaadin-email-field)::part(input-field){padding-block:0}copilot-feedback-panel :is(vaadin-select)::part(input-field){padding-inline-end:0}copilot-feedback-panel vaadin-select::part(toggle-button){align-items:center;display:flex;height:var(--copilot-size-md);justify-content:center;width:var(--copilot-size-md)}copilot-feedback-panel vaadin-text-area textarea{line-height:var(--copilot-line-height-sm)}copilot-feedback-panel vaadin-text-area:hover::part(input-field){background:none}copilot-feedback-panel>*::part(helper-text){line-height:var(--copilot-line-height-sm);margin:0}";
var A = Object.defineProperty, P = Object.getOwnPropertyDescriptor, o = (e, t, n, l) => {
  for (var a = l > 1 ? void 0 : l ? P(t, n) : t, p = e.length - 1, r; p >= 0; p--)
    (r = e[p]) && (a = (l ? r(t, n, a) : r(a)) || a);
  return l && a && A(t, n, a), a;
};
const F = "https://github.com/vaadin", u = "https://github.com/vaadin/copilot/issues/new", T = "?template=feature_request.md&title=%5BFEATURE%5D", D = "A short, concise description of the bug and why you consider it a bug. Any details like exceptions and logs can be helpful as well.", E = "Please provide as many details as possible, this will help us deliver a fix as soon as possible.%0AThank you!%0A%0A%23%23%23 Description of the Bug%0A%0A{description}%0A%0A%23%23%23 Expected Behavior%0A%0AA description of what you would expect to happen. (Sometimes it is clear what the expected outcome is if something does not work, other times, it is not super clear.)%0A%0A%23%23%23 Minimal Reproducible Example%0A%0AWe would appreciate the minimum code with which we can reproduce the issue.%0A%0A%23%23%23 Versions%0A{versionsInfo}";
let i = class extends w {
  constructor() {
    super(), this.description = "", this.types = [
      {
        label: "Generic feedback",
        value: "feedback",
        ghTitle: ""
      },
      {
        label: "Report a bug",
        value: "bug",
        ghTitle: "[BUG]"
      },
      {
        label: "Ask a question",
        value: "question",
        ghTitle: "[QUESTION]"
      },
      {
        label: "Share an idea",
        value: "idea",
        ghTitle: "[FEATURE]"
      }
    ], this.type = this.types[0].value, this.topics = [
      {
        label: "Generic",
        value: "platform"
      },
      {
        label: "Flow",
        value: "flow"
      },
      {
        label: "Hilla",
        value: "hilla"
      },
      {
        label: "Copilot",
        value: "copilot"
      }
    ], this.topic = this.topics[0].value;
  }
  render() {
    return c`<style>
        ${x}</style
      >${this.renderContent()}${this.renderFooter()}`;
  }
  renderContent() {
    return this.message === void 0 ? c`
          <p>
            Your insights are incredibly valuable to us. Whether you’ve encountered a hiccup, have questions, or ideas
            to make our platform better, we're all ears! If you wish, leave your email, and we’ll get back to you. You
            can even share your code snippet with us for a clearer picture.
          </p>
          <vaadin-select
            .items="${this.types}"
            .value="${this.type}"
            overlay-class="alwaysVisible"
            @value-changed=${(e) => {
      this.type = e.detail.value;
    }}>
          </vaadin-select>
          <vaadin-select
            label="Feedback Topic"
            overlay-class="alwaysVisible"
            .items=${this.topics}
            .value="${this.topic}"
            .hidden=${this.type !== "feedback"}
            @value-changed=${(e) => {
      this.topic = e.detail.value;
    }}>
          </vaadin-select>
          <vaadin-text-area
            .value="${this.description}"
            @keydown=${this.keyDown}
            @focus=${() => {
      this.descriptionField.invalid = !1, this.descriptionField.placeholder = "";
    }}
            @value-changed=${(e) => {
      this.description = e.detail.value;
    }}
            label="Tell Us More"
            helper-text="Describe what you're experiencing, wondering about, or envisioning. The more you share, the better we can understand and act on your feedback"></vaadin-text-area>
          <vaadin-text-field
            @keydown=${this.keyDown}
            @value-changed=${(e) => {
      this.email = e.detail.value;
    }}
            .required=${this.type === "question"}
            id="email"
            value="${f.userInfo?.email}"
            label="Your Email${this.type === "question" ? "" : " (Optional)"}"
            helper-text="Leave your email if you’d like us to follow up, we’d love to keep the conversation going."></vaadin-text-field>
        ` : c`<p>${this.message}</p>`;
  }
  renderFooter() {
    return this.message === void 0 ? c`
          <div class="dialog-footer">
            <button
              style="margin-inline-end: auto"
              @click="${() => {
      d.emit("system-info-with-callback", {
        callback: (e) => this.openGithub(e, this),
        notify: !1
      });
    }}">
              <span class="prefix">${$.github}</span>
              Create GitHub Issue
            </button>
            <button @click="${this.close}">Cancel</button>
            <button class="primary" @click="${this.submit}" .disabled=${this.type === "question" && !this.email}>
              Submit
            </button>
          </div>
        ` : c` <div class="footer">
          <vaadin-button @click="${this.close}">Close</vaadin-button>
        </div>`;
  }
  close() {
    v.closePanel("copilot-feedback-panel");
  }
  submit() {
    if (h("feedback", { github: !1, type: this.type, topic: this.topic }), this.description.trim() === "") {
      this.descriptionField.invalid = !0, this.descriptionField.placeholder = "Please tell us more before sending", this.descriptionField.value = "";
      return;
    }
    const e = {
      description: this.description,
      email: this.email,
      type: this.type,
      topic: this.topic
    };
    d.emit("system-info-with-callback", {
      callback: (t) => y(`${m}feedback`, { ...e, versions: t }),
      notify: !1
    }), this.parentNode?.style.setProperty("--section-height", "150px"), this.message = "Thank you for sharing feedback.";
  }
  keyDown(e) {
    (e.key === "Backspace" || e.key === "Delete") && e.stopPropagation();
  }
  openGithub(e, t) {
    if (h("feedback", { github: !0, type: this.type, topic: this.topic }), this.type === "idea") {
      window.open(`${u}${T}`);
      return;
    }
    if (this.type === "feedback") {
      window.open(`${F}/${this.topic}/issues/new`);
      return;
    }
    const n = e ? e.replace(/\n/g, "%0A") : "Activate Copilot to include version info.", l = `${t.types.find((r) => r.value === this.type)?.ghTitle}`, a = t.description !== "" ? t.description : D, p = E.replace("{description}", a).replace("{versionsInfo}", n);
    window.open(`${u}?title=${l}&body=${p}`, "_blank")?.focus();
  }
};
o([
  s()
], i.prototype, "description", 2);
o([
  s()
], i.prototype, "type", 2);
o([
  s()
], i.prototype, "topic", 2);
o([
  s()
], i.prototype, "email", 2);
o([
  s()
], i.prototype, "message", 2);
o([
  s()
], i.prototype, "types", 2);
o([
  s()
], i.prototype, "topics", 2);
o([
  g("vaadin-text-area")
], i.prototype, "descriptionField", 2);
i = o([
  k("copilot-feedback-panel")
], i);
const b = {
  header: "Help Us Improve!",
  tag: "copilot-feedback-panel",
  individual: !0
}, C = {
  init(e) {
    e.addPanel(b);
  }
};
window.Vaadin.copilot.plugins.push(C);
v.addPanel(b);
export {
  i as CopilotFeedbackPanel
};
