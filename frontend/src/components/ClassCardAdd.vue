<template>
  <div class="card">
    <header>
      <input v-model="className" name="className" placeholder="New class name"/>
    </header>
    <hr/>
        <div class="card-superclass">
          <p class="title">Superclass:</p>
          <p class="superclass">
            <input v-model="superClass" type="text" name="superClass" placeholder="Superclass name" list="classes"/>
            <datalist id="classes">
              <option v-for="cls in classes" :key="cls.className">{{cls.className}}</option>
            </datalist>
          </p>
        </div>
    <div>
      <button class="primary save-new-class" @click="onAdd(className, superClass)">Add class</button>
    </div>
  </div>
</template>

<script>
import {ref, toRefs} from "vue";

export default {
  name: "ClassCardAdd",
  props: {
    classes: {type: Array, required: true},
  },
  emits: ["add"],
  setup(props, {emit}) {
    const { classes } = toRefs(props)
    const className = ref("")
    const superClass = ref("")
    const onAdd = async(className, superClass) => {
      await emit("add", {'className': className, 'superClass': superClass});
    }
    return {
      className,
      superClass,
      classes,
      onAdd,
    }
  }
}
</script>

<style scoped>
.card {
  min-width: 8em;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 3px;
  border: 1px solid #d3d3d3;
  padding: 16px;
  transition: box-shadow 0.4s;
}

.card h2 {
  text-align: center;
}

.card hr {
  display: block;
  margin: 8px 0;
  border-top: solid 1px #d2d2d2;
  width: 100%;
}

.card header {
  display: flex;
  justify-content: center;
}

.delete {
  color: #c93b3b;
  background-color: #f1f1f1;
  border-radius: 16px;
  padding: 0 8px;
  margin-left: 8px;
}

.delete:hover {
  cursor: pointer;
  color: #f1f1f1;
  background-color: #c93b3b;
}

.title {
  padding-top: 8px;
  padding-bottom: 8px;
  text-transform: uppercase;
  color: #3d3d3d;
  font-size: 12px;
}

.card-superclass {
  margin-bottom: 4px;
}

.superclass:before {
  content: "\2191";
  padding-right: 4px;
  color: #868686;
}

.subclasses {
  list-style-type: none;
}

.subclass:not(:last-child) {
  padding-bottom: 6px;
}

.subclass:before {
  content: "\2193";
  padding-right: 4px;
  color: #868686;
}

.superclass, .subclass {
  font-size: 15px;
}

input {
  height: 30px;
  padding-left: 8px;
  padding-right: 8px;
}

input[name=className] {
  width: 100%;
}

.save-new-class {
  width: 100%;
  height: 30px;
  margin-top: 8px;
}

</style>