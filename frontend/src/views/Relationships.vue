<template>
  <div id="relationship-cards">
    <div v-for="relationship in relationships" :key="relationship.name" class="card">
      <header>
        <h2>{{ relationship.name }}</h2>
        <div v-if="editing" class="delete" @click="onDelete(relationship.name)">&cross;</div>
      </header>
      <hr/>
      <div>
        <p class="title">Classes:</p>
        <ul class="classes">
          <li class="class">
            <p>{{ relationship.className1 }}</p>
          </li>
          <li class="class">
            <p>{{ relationship.className2 }}</p>
          </li>
        </ul>
      </div>
    </div>
    <div v-if="editing && !isLoading" class="card">
      <header>
        <input v-model="newRelationship" name="newRelationship" placeholder="New relationship name"/>
      </header>
      <hr/>
      <div>
        <p class="title">Classes:</p>
        <ul class="classes">
          <li class="class">
            <input v-model="newRelationshipClass1" type="text" name="newRelationshipClass1" placeholder="Left class" list="classes" autocomplete="off"/>
          </li>
          <li class="class">
            <input v-model="newRelationshipClass2" type="text" name="newRelationshipClass2" placeholder="Right class" list="classes" autocomplete="off"/>
          </li>
        </ul>
        <datalist id="classes">
          <option v-for="cls in classes" :key="cls.className">{{ cls.className }}</option>
        </datalist>
      </div>
      <div>
        <button class="primary save-new-relationship" @click="addRelationship()">Add relationship</button>
      </div>
    </div>
    <div v-if="editing && !isLoading" class="card">
      <header>
        <input v-model="oldRelationship" name="typeRelationship" placeholder="Relationship to update"/>
        <input v-model="updateRelationshipName" name="updateRelationship" placeholder="New relationship name"/>
      </header>
      <hr/>
      <div>
        <p class="title">Classes:</p>
        <ul class="classes">
          <li class="class">
            <input v-model="updateRelationshipClass1" type="text" name="newRelationshipClass1" placeholder="Left class" list="classes" autocomplete="off"/>
            <input v-model="updateRelationshipClass2" type="text" name="newRelationshipClass2" placeholder="Right class" list="classes" autocomplete="off"/>
          </li>
        </ul>

      </div>
      <div>
        <button class="primary save-new-relationship" @click="updateRelationship()">Update relationship</button>
      </div>
    </div>
  </div>
</template>

<script>
import {computed, inject, onActivated, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRoute} from "vue-router";

export default {
  name: "Relationships",
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const relationships = ref([]);
    const classes = ref([]);
    const route = useRoute()
    const newRelationship = ref("");
    const oldRelationship = ref("");
    const newRelationshipClass1 = ref("");
    const newRelationshipClass2 = ref("");
    const updateRelationshipName = ref("");
    const updateRelationshipClass1 = ref("");
    const updateRelationshipClass2 = ref("");

    const fetchRelationships = async () => {
      relationships.value = []
      await store.dispatch('setLoading', {loadingText: "Loading relationships…", loadingId: 800, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      relationships.value = response.data.data;
      await store.dispatch('setLoading', {loadingId: 800, isLoading: false});
    }

    onActivated(async () => await fetchRelationships())

    watch(() => store.getters.branch, async () => {
      if (route.name === "Relationships") {
        await fetchRelationships();
      }
    })

    const fetchClasses = async (branch) => {
      classes.value = []
      await store.dispatch('setLoading', {loadingText: "Loading relationships…", loadingId: 1400, isLoading: true});
      const response = branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/classes?branch=${branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/classes?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      const classesToAdd = response.data.data;
      while (classesToAdd.length > 0) {
        console.log(classesToAdd)
        const cls = classesToAdd.shift()
        classes.value.push(cls)
        if (cls.subclasses && cls.subclasses.length > 0) {
          cls.subclasses.forEach(it => {
            it["superClass"] = cls
            classesToAdd.push(it);
          })
        }
      }
      await store.dispatch('setLoading', {loadingId: 1400, isLoading: false});
    }

    onActivated(async () => await fetchClasses(store.getters.branch))

    watch(() => store.getters.branch, async (branch) => {
      if (route.name === "Relationships") {
        await fetchClasses(branch);
      }
    })

    const onDelete = async (relationship) => {
      await store.dispatch('setLoading', {loadingText: `Deleting relationship ${relationship}…`, loadingId: 1600, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/relationship/${relationship}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.delete(endpoint)
        console.log(response)
        await fetchRelationships()
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1600, isLoading: false});
    }

    const addRelationship = async () => {
      await store.dispatch('setLoading', {loadingText: `Adding relationship ${newRelationship.value}…`, loadingId: 1700, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/relationship/${newRelationship.value}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.post(endpoint, {className1: newRelationshipClass1.value, className2: newRelationshipClass2.value})
        console.log(response)
        newRelationship.value = "";
        newRelationshipClass1.value = "";
        newRelationshipClass2.value = "";
        await fetchRelationships()
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1700, isLoading: false});
    }

    const updateRelationship = async () => {
      await store.dispatch('setLoading', {loadingText: `Adding relationship ${oldRelationship.value}…`, loadingId: 1700, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/relationship/${oldRelationship.value}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.put(endpoint, {newRelationshipName: updateRelationshipName.value, className1: updateRelationshipClass1.value, 
          className2: updateRelationshipClass2.value})
        console.log(response)
        oldRelationship.value = "";
        updateRelationshipName.value = "";
        updateRelationshipClass1.value = "";
        updateRelationshipClass2.value = "";
        await fetchRelationships()
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1700, isLoading: false});
    }

    return {
      relationships,
      editing: computed(() => store.getters.branch),
      isLoading: computed(() => store.getters.isLoading),
      onDelete,
      addRelationship,
      updateRelationship,
      updateRelationshipName,
      newRelationshipClass1,
      newRelationshipClass2,
      newRelationship,
      updateRelationshipClass1,
      updateRelationshipClass2,
      oldRelationship,
      classes,
    }
  }
}
</script>

<style scoped>
#relationship-cards {
  width: 70%;
  margin: 0 auto;
  display: flex;
  flex-flow: row wrap;
  align-items: stretch;
  align-content: stretch;
  gap: 16px;
}

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

.classes {
  list-style-type: none;
}

.class:not(:last-child) {
  padding-bottom: 6px;
}

.class {
  font-size: 15px;
}

.save-new-relationship {
  width: 100%;
  height: 30px;
  margin-top: 8px;
}
</style>