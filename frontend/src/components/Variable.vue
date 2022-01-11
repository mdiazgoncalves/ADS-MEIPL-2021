<template>
  <div class="variable-box" v-if="variable !== undefined">
    <div class="delete delete-variable" @click="$emit('delete')" v-if="showDelete">Delete variable &cross;</div>
    <div class="row">
      <label>Type: </label>
      <select :value="variable.type"
              @input="$emit('reset', ({ type: $event.target.value, name: $event.target.value + (n[$event.target.value] + 1), output: !!variable.output }))"
              style="margin-right: 16px">
        <option disabled selected value> -- select an option --</option>
        <option value="individual">Individual</option>
        <option value="class">Class</option>
        <option value="relationship">Relationship</option>
      </select>
      <label>Label: </label>
      <input name="variableName" autocomplete="off"
             placeholder="Name"
             :value="variable.name"
             @input="change(variable, v => v.name = $event.target.value)"
             style="margin-right: 14px">
      <label>Output: </label>
      <input type="checkbox" :checked="variable.output"
             @input="change(variable, v => v.output = $event.target.checked)">
    </div>
    <template v-if="variable.type">
      <hr>
      <div class="filter" v-for="(filter, index) in variable.filters"
           :key="filter">
        <div class="delete" @click="deleteFilter(variable, index)">&cross;</div>
        <template v-if="filter.type === 'individual_has_class'">
          <label>Has class </label>
          <input placeholder="Class name"
                 :value="filter.class"
                 @change="change(variable, v => v.filters[index].class = $event.target.value)"
                 autocomplete="off" list="classes">
        </template>
        <template v-if="filter.type === 'individual_has_relationship'">
          <label>Is on the </label>
          <select style="width: 100px;" :value="filter.side"
                  @input="change(variable, v => v.filters[index].side = $event.target.value)">
            <option value="left" selected>Left</option>
            <option value="right">Right</option>
          </select>
          <label> in relationship </label>
          <input placeholder="Relationship"
                 :value="filter.relationship"
                 @change="change(variable, v => v.filters[index].relationship = $event.target.value)"
                 autocomplete="off"
                 list="relationships">
          <label> with individual </label>
          <input name="individual" placeholder="Individual"
                 :value="filter.individual2"
                 @change="change(variable, v => v.filters[index].individual2 = $event.target.value)"
                 autocomplete="off" list="individuals">
        </template>
        <template v-if="filter.type === 'class_has_superclass'">
          <label>Has superclass </label>
          <input placeholder="Superclass"
                 :value="filter.superclass"
                 @change="change(variable, v => v.filters[index].superclass = $event.target.value)"
                 autocomplete="off" list="classes">
        </template>
        <template v-if="filter.type === 'class_has_subclass'">
          <label>Has subclass </label>
          <input placeholder="Subclass"
                 :value="filter.subclass"
                 @change="change(variable, v => v.filters[index].subclass = $event.target.value)"
                 autocomplete="off" list="classes">
        </template>
        <template v-if="filter.type === 'class_has_individual'">
          <label>Has individual </label>
          <input placeholder="Individual"
                 :value="filter.individual"
                 @change="change(variable, v => v.filters[index].individual = $event.target.value)"
                 autocomplete="off" list="individuals">
        </template>
        <template v-if="filter.type === 'relationship_has_left'">
          <label>Has left class </label>
          <input placeholder="Class"
                 :value="filter.class"
                 @change="change(variable, v => v.filters[index].class = $event.target.value)"
                 autocomplete="off" list="classes">
        </template>
        <template v-if="filter.type === 'relationship_has_right'">
          <label>Has right class </label>
          <input placeholder="Class"
                 :value="filter.class"
                 @change="change(variable, v => v.filters[index].class = $event.target.value)"
                 autocomplete="off" list="classes">
        </template>
      </div>
      <datalist id="classes">
        <option v-for="cls in classes" :key="cls.className">{{ cls.className }}</option>
      </datalist>
      <datalist id="relationships">
        <option v-for="relationship in relationships" :key="relationship.name">{{ relationship.name }}</option>
      </datalist>
      <datalist id="individuals">
        <option v-for="individual in individuals" :key="individual.individualName">{{
            individual.individualName
          }}
        </option>
      </datalist>
      <div>
        <label>Add filter: </label>
        <select @input="addFilter(variable, $event.target.value); $event.target.value = ''">
          <option disabled selected value> -- select an option --</option>
          <option value="individual_has_class" v-if="variable.type === 'individual'">Has class</option>
          <option value="individual_has_relationship" v-if="variable.type === 'individual'">Is in relationship</option>
          <option value="class_has_superclass" v-if="variable.type === 'class'">Has superclass</option>
          <option value="class_has_subclass" v-if="variable.type === 'class'">Has subclass</option>
          <option value="class_has_individual" v-if="variable.type === 'class'">Has individual</option>
          <option value="relationship_has_left" v-if="variable.type === 'relationship'">Has left class</option>
          <option value="relationship_has_right" v-if="variable.type === 'relationship'">Has right class</option>
        </select>
      </div>
    </template>
  </div>
</template>

<script>

export default {
  name: "Variable",
  props: {
    variable: Object,
    n: Object,
    showDelete: Boolean,
    individuals: Array,
    classes: Array,
    relationships: Array,
  },
  setup(props, {emit}) {
    const change = (variable, change) => {
      const clone = JSON.parse(JSON.stringify(variable))
      console.log("cloned", clone)
      change(clone)
      console.log("result", clone)
      emit('changeVar', clone)
    }

    const addFilter = (variable, type) => {
      const clone = JSON.parse(JSON.stringify(variable))
      console.log("cloned", clone)
      if (clone.filters === undefined) {
        clone.filters = []
      }
      clone.filters.push({type: type})
      console.log("result", clone)
      emit('changeVar', clone)
    }

    const deleteFilter = (variable, index) => {
      const clone = JSON.parse(JSON.stringify(variable))
      console.log("cloned", clone)
      if (clone.filters === undefined) {
        clone.filters = []
      }
      clone.filters.splice(index, 1)
      console.log("result", clone)
      emit('changeVar', clone)
    }

    return {
      change,
      addFilter,
      deleteFilter,
    }
  }
}
</script>

<style scoped>

.delete-variable {
  position: absolute;
  right: 8px;
  top: 16px;
}

.variable-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  margin-top: 4px;
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #d3d3d3;
  background-color: white;
  margin-bottom: 8px;
}

label {
  font-weight: 500;
}

.filter .delete {
  display: inline-block;
  margin-left: 0;
  margin-right: 8px;
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
}

hr {
  height: 1px;
  background-color: #ccc;
  border: none;
}
</style>