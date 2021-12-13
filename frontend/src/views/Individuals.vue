<template>
  <div id="individuals-cards">
    <div v-for="individual in individuals" :key="individual.individualName" class="card"
         :id="individual.individualName">
      <header>
        <div class="class-name">{{ individual.className }}</div>
        <div class="individual-name-delete">
          <h2>{{ individual.individualName }}</h2>
          <div v-if="editing" class="delete" @click="onDelete(individual.individualName)">&cross;</div>
        </div>
      </header>
      <hr/>
      <div>
        <p class="title">Relationships:</p>
        <ul class="relationships">
          <li class="relationship" v-for="relationship in individual.relationships"
              :key="relationship.relationshipName">
            <div class="relationship-name">{{ relationship.relationshipName }}:</div>
            <div class="relationship-individual">
              <router-link :to="'#'+relationship.individual2">{{ relationship.individual2 }}</router-link>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div v-if="editing && !isLoading" class="card">
      <header>
        <input v-model="newIndividualClassName" name="newIndividualClassName" placeholder="Class name" list="classes"
               autocomplete="off"/>
        <datalist id="classes">
          <option v-for="cls in classes" :key="cls.className">{{ cls.className }}</option>
        </datalist>
        <input v-model="newIndividual" name="newIndividual" placeholder="New individual name"/>
      </header>
      <hr/>
      <div>
        <p class="title">Relationships:</p>
        <ul class="relationships">
          <li class="relationship" v-for="(relationship, index) in newIndividualRelationships" :key="index">
            <input v-model="relationship.relationshipName" type="text" :name="`relationship${index}Name`"
                   placeholder="Relationship name" :list="`relationships${index}`" autocomplete="off"/>
            <datalist :id="`relationships${index}`">
              <option v-for="relationship in relationships.filter(it => it.className1 === newIndividualClassName)"
                      :key="relationship.name">{{ relationship.name }}
              </option>
            </datalist>
            <input v-model="relationship.individual2" type="text" :name="`relationship${index}Individual2`"
                   placeholder="Individual name" :list="`individuals${index}`" autocomplete="off"/>
            <datalist :id="`individuals${index}`">
              <option
                  v-for="individual in individuals.filter(it => it.className === relationships.find(r => r.name === relationship.relationshipName)?.className2)"
                  :key="individual.individualName">{{ individual.individualName }}
              </option>
            </datalist>
          </li>
        </ul>
      </div>
      <div>
        <button class="primary save-new-individual" @click="addIndividual()">Add individual</button>
      </div>
    </div>
  </div>
</template>

<script>
import {computed, inject, onActivated, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRoute} from "vue-router";

export default {
  name: "Individuals",
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const individuals = ref([]);
    const relationships = ref([]);
    const classes = ref([]);
    const route = useRoute()
    const newIndividual = ref("");
    const newIndividualClassName = ref("");
    const newIndividualRelationships = ref([{}]);

    const fetchIndividuals = async () => {
      individuals.value = []
      await store.dispatch('setLoading', {loadingText: "Loading individuals…", loadingId: 700, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      individuals.value = response.data.data.sort((a, b) => a.className.localeCompare(b.className));
      await store.dispatch('setLoading', {loadingId: 700, isLoading: false});
    }

    onActivated(async () => await fetchIndividuals())

    watch(() => store.getters.branch, async () => {
      if (route.name === "Individuals") {
        await fetchIndividuals();
      }
    })

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

    const fetchRelationships = async () => {
      relationships.value = []
      await store.dispatch('setLoading', {loadingText: "Loading individuals…", loadingId: 900, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      relationships.value = response.data.data;
      await store.dispatch('setLoading', {loadingId: 900, isLoading: false});
    }

    onActivated(async () => await fetchRelationships())

    watch(() => store.getters.branch, async () => {
      if (route.name === "Individuals") {
        await fetchRelationships();
      }
    })

    const fetchClasses = async (branch) => {
      classes.value = []
      await store.dispatch('setLoading', {loadingText: "Loading individuals…", loadingId: 1000, isLoading: true});
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
      await store.dispatch('setLoading', {loadingId: 1000, isLoading: false});
    }

    onActivated(async () => await fetchClasses(store.getters.branch))

    watch(() => store.getters.branch, async (branch) => {
      if (route.name === "Individuals") {
        await fetchClasses(branch);
      }
    })

    watch(newIndividualRelationships, it => {
      const lastRelationship = it.at(-1);
      if (lastRelationship.relationshipName !== undefined && lastRelationship.relationshipName.length > 0) {
        newIndividualRelationships.value.push({});
      }
      const emptyIndex = it.slice(0, -1).findIndex(it => it.relationshipName === undefined || it.relationshipName.length === 0)
      if(emptyIndex !== -1) {
        newIndividualRelationships.value.splice(emptyIndex, 1)
      }
    }, {deep: true})

    const onDelete = async (individual) => {
      await store.dispatch('setLoading', {loadingText: `Deleting individual ${individual}…`, loadingId: 1100, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/individual/${individual}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if (store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.delete(endpoint)
        console.log(response)
        await fetchIndividuals()
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1100, isLoading: false});
    }

    const addIndividual = async () => {
      await store.dispatch('setLoading', {loadingText: `Adding individual ${newIndividual.value}…`, loadingId: 1200, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/individual/${newIndividual.value}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.post(endpoint, {className: newIndividualClassName.value, relationships: newIndividualRelationships.value.filter(it => it.relationshipName !== undefined && it.relationshipName.length > 0)})
        console.log(response)
        newIndividual.value = "";
        newIndividualClassName.value = "";
        newIndividualRelationships.value = [{}];
        await fetchIndividuals()
      } catch (e) {
        console.log(e.response)
      }
      await store.dispatch('setLoading', {loadingId: 1200, isLoading: false});
    }

    return {
      newIndividual,
      individuals,
      classes,
      relationships,
      editing: computed(() => store.getters.branch),
      isLoading: computed(() => store.getters.isLoading),
      onDelete,
      newIndividualClassName,
      newIndividualRelationships,
      addIndividual,
    }
  }
}
</script>

<style scoped>
#individuals-cards {
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
  flex-direction: column;
  gap: 4px;
  justify-content: center;
}

.individual-name-delete {
  display: flex;
  justify-content: center;
}

.class-name {
  font-size: 12px;
  color: #494949;
  text-transform: uppercase;
  text-align: center;
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

.relationships {
  list-style-type: none;
}

.relationship:not(:last-child) {
  padding-bottom: 6px;
}

.relationship-name {
  font-weight: 500;
}

.relationship {
  font-size: 15px;
  display: flex;
  gap: 8px;
}

.save-new-individual {
  width: 100%;
  height: 30px;
  margin-top: 8px;
}
</style>