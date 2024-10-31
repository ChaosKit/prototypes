const state = {
  selectedIndex: {
    blend: undefined,
    formula: undefined
  },
  blends: [
    {
      formulas: [
        "DeJong",
        "Not DeJong",
        "Linear",
        "DeJong",
        "Not DeJong",
        "Linear",
        "DeJong",
        "Not DeJong",
        "Linear"
      ]
    },
    {
      formulas: ["DeJong"]
    }
  ],
  pickerOpen: false,
  allFormulas: [
    "DeJong",
    "Linear",
    "Tinkerbell",
    "Something",
    "DeJong",
    "Linear",
    "Tinkerbell",
    "Something",
    "DeJong",
    "Linear",
    "Tinkerbell",
    "Something",
    "DeJong",
    "Linear",
    "Tinkerbell",
    "Something"
  ]
};

function hasAncestor(element, selector) {
  do {
    if (element.matches(selector)) {
      return true;
    }
    element = element.parentElement;
  } while (element != null);
  return false;
}

const app = new Vue({
  el: ".sidebar",
  data: state,
  methods: {
    selectBlend(index) {
      this.selectedIndex.blend = index;
      this.selectedIndex.formula = undefined;
    },
    selectFormula(blend, formula, event) {
      event.stopPropagation();
      this.selectedIndex.blend = blend;
      this.selectedIndex.formula = formula;
    },
    openPicker() {
      this.pickerOpen = true;
    },
    addBlend(formula) {
      this.blends.push({ formulas: [formula] });
      this.pickerOpen = false;
    },
    removeBlend() {
      if (this.selectedIndex.blend == null) return;
      this.blends.splice(this.selectedIndex.blend, 1);
      this.selectedIndex.blend = undefined;
    },
    defocus(e) {
      if (
        hasAncestor(e.target, "[aria-label='Add blend']") ||
        hasAncestor(e.target, ".blend")
      ) {
        return;
      }
      this.selectedIndex.blend = undefined;
      this.selectedIndex.formula = undefined;
      this.pickerOpen = false;
    }
  }
});
