<template>
  <section id="queries-page">
    <div class="filter">
<!--      <h2>Query Filters:</h2>-->
      <form class="simple-filter" @submit.prevent="computeAndExecute()">
        <div class="query-section">
          <h2>Query By Class:</h2>
          <div class="simple-filter-fields">
            <input name="className" placeholder="Class Name" v-model="className" autocomplete="off" list="classes">
            <datalist id="classes">
              <option v-for="cls in classes" :key="cls.className">{{ cls.className }}</option>
            </datalist>
          </div>
        </div>
        <div class="query-section">
          <h2>Query By Relationship:</h2>
          <div class="simple-filter-fields">
            <div class="side-fields">
              <input placeholder="Relationship Name" v-model="relationship" autocomplete="off" list="relationships">
              <datalist id="relationships">
                <option v-for="relationship in relationships.filter(it => it.className1 === className)"
                        :key="relationship.name">{{ relationship.name }}
                </option>
              </datalist>
              <input placeholder="Individual Name" v-model="individual" autocomplete="off" list="individuals">
              <datalist id="individuals">
                <option
                    v-for="individual in individuals.filter(it => it.className === relationships.find(r => r.name === relationship.relationshipName)?.className2)"
                    :key="individual.individualName">{{ individual.individualName }}
                </option>
              </datalist>
            </div>
          </div>
        </div>
        <div class="side-buttons">
          <input type="submit" class="button primary" value="Execute">
          <input type="button" class="button primary-outline" value="Advanced queries" @click="showAdvanced = !showAdvanced">
        </div>
      </form>
      <form class="query-input-container" @submit.prevent="execute()" v-if="showAdvanced">
        <input type="text" class="query-input"
               placeholder="Input your SQWRL query [e.g. #Car(?x) ^ #color(?x, #Blue) -> sqwrl:select(?x)]"
               v-model="query"/>
        <input type="submit" value="Execute" class="primary"/>
      </form>
    </div>
    <div id="query-results">
<!--      <h2>Query Results:</h2>-->
      <div id="individuals-cards">
        <div v-for="individual in resultIndividuals" :key="individual">
          <router-link :to="`/individuals#${individual}`" class="card">
            <header>
              <div class="individual-name-delete">
                <h2>{{ individual }}</h2>
              </div>
            </header>
          </router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {inject, onActivated, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRoute} from "vue-router";

export default {
  name: "Queries",
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const query = ref("#Bebida(?x) -> sqwrl:select(?x)")
    const individuals = ref([])
    const resultIndividuals = ref([])
    const showAdvanced = ref(false)
    const className = ref("")
    const relationship = ref("")
    const individual = ref("")
    const classes = ref([])
    const relationships = ref([])
    const route = useRoute()

    const execute = async () => {
      resultIndividuals.value = []
      await store.dispatch('setLoading', {loadingText: "Executing query…", loadingId: 1801, isLoading: true});
      try {
        const url = store.getters.branch ? `${process.env.VUE_APP_BACKEND}/query?branch=${store.getters.branch}` : `${process.env.VUE_APP_BACKEND}/query?branch=main`
        const response = await axios.post(url, query.value, {
          headers: {
            "Content-Type": "text/plain"
          }
        })
        console.log(response)
        resultIndividuals.value = response.data.data
      } catch (e) {
        console.log(e.response)
      }
      await store.dispatch('setLoading', {loadingId: 1801, isLoading: false});
    }

    const computeAndExecute = async () => {
      let resultQuery = ""
      if(className.value.length > 0) {
        resultQuery += `#${className.value}(?x)`
        if (relationship.value.length > 0 && individual.value.length > 0) {
          resultQuery += ` ^ #${relationship.value}(?x, #${individual.value})`
        }
        resultQuery += " -> sqwrl:select(?x)"
      }
      query.value = resultQuery
      await execute()
    }

    const fetchIndividuals = async () => {
      individuals.value = []
      await store.dispatch('setLoading', {loadingText: "Loading queries…", loadingId: 701, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      individuals.value = response.data.data.sort((a, b) => a.className.localeCompare(b.className));
      await store.dispatch('setLoading', {loadingId: 701, isLoading: false});
    }

    onActivated(async () => await fetchIndividuals())

    watch(() => store.getters.branch, async () => {
      if (route.name === "Queries") {
        await fetchIndividuals();
      }
    })

    const fetchRelationships = async () => {
      relationships.value = []
      await store.dispatch('setLoading', {loadingText: "Loading queries…", loadingId: 901, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/relationships?branch=main`);
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      relationships.value = response.data.data;
      await store.dispatch('setLoading', {loadingId: 901, isLoading: false});
    }

    onActivated(async () => await fetchRelationships())

    watch(() => store.getters.branch, async () => {
      if (route.name === "Queries") {
        await fetchRelationships();
      }
    })

    const fetchClasses = async (branch) => {
      classes.value = []
      await store.dispatch('setLoading', {loadingText: "Loading queries…", loadingId: 1001, isLoading: true});
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
      await store.dispatch('setLoading', {loadingId: 1001, isLoading: false});
    }

    onActivated(async () => await fetchClasses(store.getters.branch))

    watch(() => store.getters.branch, async (branch) => {
      if (route.name === "Queries") {
        await fetchClasses(branch);
      }
    })

    return {
      query,
      execute,
      computeAndExecute,
      individuals,
      resultIndividuals,
      showAdvanced,
      className,
      relationship,
      individual,
      relationships,
      classes,
    }
  }
}
</script>

<style scoped>
#queries-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter {
  width: 70%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter > h2 {
  padding-bottom: 12px;
  padding-left: 4px;
  font-size: 20px;
}

.simple-filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background-color: white;
  padding: 16px;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 3px;
  border: 1px solid #d3d3d3;
  transition: box-shadow 0.4s;
}

.simple-filter-fields {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
}

input[name=className] {
  display: block;
  width: 404px;
}

.side-fields {
  display: flex;
  gap: 4px;
}

.side-buttons {
  display: flex;
  gap: 4px;
}

.side-buttons > input {
  width: 200px;
}

.side-buttons > input[type=button] {
  border: solid 1px #4b4b4b;
  color: #4b4b4b;
}

.side-buttons > input[type=button]:hover {
  border: solid 1px #4b4b4b;
  background-color: #e8e8e8;
  color: #4b4b4b;
}

.query-input-container {
  width: 100%;
  margin: 0 auto;
  display: flex;
  gap: 4px;
}

.query-input {
  flex: 1;
}

#query-results {
  width: 70%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

#query-results > h2 {
  font-size: 20px;
  padding-left: 4px;
}

#individuals-cards {
  display: flex;
  flex-flow: row wrap;
  align-items: stretch;
  align-content: stretch;
  gap: 8px;
}

.card h2 {
  text-align: center;
}

.card {
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 32px;
  border: 1px solid #d3d3d3;
  padding: 16px;
  transition: box-shadow 0.2s, border-color 0.2s, color 0.2s;
}

.card:hover {
  box-shadow: rgba(0, 0, 0, 0.08) 0 3px 5px;
  border-color: var(--primary);
  color: var(--primary);
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
</style>