<template>
  <ClassCardsGrid :classes="classes" @delete="deleteClass" @add="addClass"/>
</template>

<script>
import ClassCardsGrid from "@/components/ClassCardsGrid";
import {inject, onActivated, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRoute} from "vue-router";

export default {
  name: "Classes",
  components: {ClassCardsGrid},
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const classes = ref([]);
    const route = useRoute()

    const fetchClasses = async (branch) => {
      classes.value = []
      await store.dispatch('setLoading', {loadingText: "Loading classes…", loadingId: 400, isLoading: true});
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
      await store.dispatch('setLoading', {loadingId: 400, isLoading: false});
    }

    onActivated(async () => await fetchClasses(store.getters.branch))

    watch(() => store.getters.branch, async (branch) => {
      if (route.name === "Classes") {
        await fetchClasses(branch);
      }
    })

    const deleteClass = async (className) => {
      await store.dispatch('setLoading', {loadingText: `Deleting class ${className}…`, loadingId: 500, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/class/${className}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.delete(endpoint)
        console.log(response)
        await fetchClasses(store.getters.branch)
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 500, isLoading: false});
    }

    const addClass = async ({className, superClass}) => {
      await store.dispatch('setLoading', {loadingText: `Adding class ${className}…`, loadingId: 600, isLoading: true});
      try {
        let endpoint = `${process.env.VUE_APP_BACKEND}/class/${className}?branch=${store.getters.branch}&commit=${store.getters.commit}`
        if(store.getters.branch === "main") {
          endpoint += `&token=${store.getters.token}`
        }
        const response = await axios.post(endpoint, {superClassName: superClass === undefined || superClass.length === 0 ? null : superClass})
        console.log(response)
        await fetchClasses(store.getters.branch)
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 600, isLoading: false});
    }

    return {
      classes,
      deleteClass,
      addClass,
    }
  },
}
</script>