<template>
  <section id="class-cards">
    <div class="grid-container">
      <ClassCard v-for="(_class, index) in classes" :key="index" :_class="_class" @delete="onDelete" />
      <ClassCardAdd v-if="isEditing && !isLoading" :classes="classes" @add="onAdd"></ClassCardAdd>
    </div>
  </section>
</template>

<script>
import ClassCard from "@/components/ClassCard";
import {computed, toRefs} from "vue";
import {useStore} from "vuex";
import ClassCardAdd from "@/components/ClassCardAdd";

export default {
  name: "ClassCardsGrid",
  props: {
    classes: Array,
  },
  emits: ['delete', 'add'],
  components: {
    ClassCardAdd,
    ClassCard,
  },
  setup(props, { emit }) {
    const { classes } = toRefs(props)
    const store = useStore()
    const onDelete = async(className) => {
      await emit('delete', className)
    }
    const onAdd = async(payload) => {
      await emit("add", payload);
    }
    return {
      classes,
      isEditing: computed(() => store.getters.branch != null),
      isLoading: computed(() => store.getters.isLoading),
      onDelete,
      onAdd,
    }
  }
}
</script>

<style scoped>
#class-cards {
  width: 70%;
  margin: 0 auto;
}

.grid-container {
  display: flex;
  flex-flow: row wrap;
  align-items: stretch;
  align-content: stretch;
  gap: 16px;
}
</style>