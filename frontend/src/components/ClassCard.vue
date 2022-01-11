<template>
  <div class="card" :id="_class.className">
    <header>
      <h2>{{ _class.className }}</h2>
      <div v-if="editing" class="delete" @click="onDelete(_class.className)">&cross;</div>
    </header>
    <hr/>
    <div v-if="_class.superClass" class="card-superclass">
      <p class="title">Superclass:</p>
      <p class="superclass">
        <router-link :to="'#'+_class.superClass.className">{{ _class.superClass.className }}</router-link>
      </p>
    </div>
    <div v-if="_class.subclasses && _class.subclasses.length > 0">
      <p class="title">Subclasses:</p>
      <ul class="subclasses">
        <li v-for="(subclass, index) in _class.subclasses" :key="index" class="subclass">
          <router-link :to="'#'+subclass.className">{{ subclass.className }}</router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import {useRoute} from 'vue-router'
import {computed, watch} from "vue";
import {useStore} from "vuex";

export default {
  name: "ClassCard",
  props: {
    _class: {type: Object, required: true},
  },
  emits: ['delete'],
  setup(props, {emit}) {
    const route = useRoute()
    const store = useStore()

    watch(() => route.hash.substr(1), (value, oldValue) => {
      const selected = document.getElementById(value);
      if (selected) {
        selected.style.boxShadow = "rgba(var(--primary-rgb), 0.5) 0 0px 0px 3px";
      }
      const prevSelected = document.getElementById(oldValue);
      if (prevSelected) {
        prevSelected.style.boxShadow = "none";
      }
    })
    const onDelete = async (className) => {
      await emit('delete', className)
    }

    return {
      editing: computed(() => store.getters.branch),
      onDelete
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

</style>