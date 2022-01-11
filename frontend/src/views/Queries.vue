<template>
  <section id="queries-page">
    <div class="filter">
      <form @submit.prevent="computeAndExecute()">
        <Variable v-for="(variable, index) in variables" :key="index" :variable="variable"
                  @changeVar="result => change(index, result)" @reset="result => change(index, result)" :n="n"
                  @delete="deleteVar(index)" :showDelete="variables.length > 1" :individuals="individuals"
                  :classes="classes" :relationships="relationships"/>
        <div class="side-buttons">
          <input type="submit" class="button primary" value="Execute">
          <input type="button" class="button white" value="Add a variable"
                 @click="variables.push({})">
          <input type="button" class="button white" value="Advanced queries"
                 @click="showAdvanced = !showAdvanced">
        </div>
      </form>
      <form class="query-input-container" @submit.prevent="execute()" v-if="showAdvanced">
        <input type="text" class="query-input"
               placeholder="Input your SQWRL query [e.g. #Car(?x) ^ #color(?x, #Blue) -> sqwrl:select(?x)]"
               v-model="queries[0]"/>
        <input type="submit" value="Execute" class="primary"/>
      </form>
    </div>
    <div id="query-results">
      <div v-for="(result, index) in results" :key="index">
        <h2 class="capitalize-first-letter" style="padding-bottom: 12px">{{`${typePlural(result.type)} (${result.variableName})`}}</h2>
        <div id="individuals-cards">
          <div v-for="name in result.results" :key="name">
            <router-link :to="`/${typePlural(result.type)}#${name}`" class="card">
              <header>
                <div class="individual-name">
                  <h2>{{ name }}</h2>
                </div>
              </header>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, onActivated, reactive, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRoute} from "vue-router";
import Variable from "@/components/Variable";

export default {
  name: "Queries",
  components: {Variable},
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const queries = reactive(["#Bebida(?x) -> sqwrl:select(?x)"])
    const individuals = ref([])
    const results = ref([])
    const showAdvanced = ref(false)
    const className = ref("")
    const relationship = ref("")
    const individual = ref("")
    const classes = ref([])
    const relationships = ref([])
    const route = useRoute()
    const variables = reactive([
      {
        output: true,
      }
    ])

    const execute = async () => {
      results.value = []
      await store.dispatch('setLoading', {loadingText: "Executing query…", loadingId: 1801, isLoading: true});
      try {
        const url = store.getters.branch ? `${process.env.VUE_APP_BACKEND}/query?branch=${store.getters.branch}` : `${process.env.VUE_APP_BACKEND}/query?branch=main`
        const response = await axios.post(url, queries, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        console.log(response)
        results.value = response.data.data.map((it, index) => {
          it.type = variables.filter(it => it.output)[index].type
          return it
        })
      } catch (e) {
        console.log(e.response)
      }
      await store.dispatch('setLoading', {loadingId: 1801, isLoading: false});
    }

    // quick function to prepend variable with either # or ?, depending on if a variable with that name is defined
    const $ = variableName => variables.find(it => it.name === variableName) ? `?${variableName}` : `#${variableName}`

    const computeAndExecute = async () => {
      let operations = []
      for (const variable of variables) {
        if (variable.type === 'individual') {
          for (const filter of variable.filters) {
            if (filter.type === 'individual_has_class') {
              operations.push(`abox:caa(${$(filter.class)}, ?${variable.name})`)
            } else if (filter.type === 'individual_has_relationship') {
              operations.push(`abox:opaa(${filter.side === 'left' ? `?${variable.name}` : `${$(filter.individual2)}`}, ${$(filter.relationship)}, ${filter.side === 'right' ? `?${variable.name}` : `${$(filter.individual2)}`})`)
            }
          }
        } else if (variable.type === 'class') {
          for (const filter of variable.filters) {
            if (filter.type === 'class_has_superclass') {
              operations.push(`tbox:sca(?${variable.name}, ${$(filter.superclass)})`)
            }
            if (filter.type === 'class_has_subclass') {
              operations.push(`tbox:sca(${$(filter.subclass)}, ?${variable.name})`)
            } else if (filter.type === 'class_has_individual') {
              operations.push(`abox:caa(?${variable.name}, ${$(filter.individual)})`)
            }
          }
        } else if (variable.type === 'relationship') {
          for (const filter of variable.filters) {
            if (filter.type === 'relationship_has_left') {
              operations.push(`tbox:opda(?${variable.name}, ${$(filter.class)})`)
            }
            if (filter.type === 'relationship_has_right') {
              operations.push(`tbox:opra(?${variable.name}, ${$(filter.class)})`)
            }
          }
        }
      }
      queries.splice(0, queries.length)
      for (const variable of variables.filter(it => it.output)) {
        queries.push(operations.join(" ^ ") + ` -> sqwrl:select(?${variable.name})`)
      }
      console.log("Queries", queries)
      await execute()
    }

    const change = (index, result) => {
      variables.splice(index, 1, result)
      console.log("Changed variable", result)
      console.log("index", index)
      console.log("variables", variables)
    }

    const reset = (index, variable) => {
      variables.splice(index, 1, variable)
      console.log("Reset variable", variable)
      console.log("index", index)
      console.log("variables", variables)
    }

    const deleteVar = (index) => {
      variables.splice(index, 1)
      console.log("Deleted variable index", index)
      console.log("variables", variables)
    }

    const n = computed(() => ({
      individual: variables.filter(it => it.type === "individual")?.length,
      class: variables.filter(it => it.type === "class").length,
      relationship: variables.filter(it => it.type === "relationship")?.length,
    }))

    const typePlural = type => ({
      individual: "individuals",
      class: "classes",
      relationship: "relationships",
    }[type])

    const fetchIndividuals = async () => {
      individuals.value = []
      await store.dispatch('setLoading', {loadingText: "Loading queries…", loadingId: 701, isLoading: true});
      const response = store.getters.branch ? await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=${store.getters.branch}`) : await axios.get(`${process.env.VUE_APP_BACKEND}/individuals?branch=main`);
      console.log("Individuals", response);
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
      queries,
      execute,
      computeAndExecute,
      individuals,
      results,
      showAdvanced,
      typePlural,
      className,
      relationship,
      individual,
      relationships,
      classes,
      variables,
      change,
      reset,
      deleteVar,
      n,
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

.side-buttons {
  display: flex;
  gap: 4px;
}

.side-buttons > input {
  width: 200px;
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

.individual-name {
  display: flex;
  justify-content: center;
}

/*.class-name {*/
/*  font-size: 12px;*/
/*  color: #494949;*/
/*  text-transform: uppercase;*/
/*  text-align: center;*/
/*}*/

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