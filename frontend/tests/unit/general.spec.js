import { shallowMount } from '@vue/test-utils'
import HelloWorld from '@/components/HelloWorld'

describe('General testing', () => {
  it('renders app', () => {
    const wrapper = shallowMount(HelloWorld)
    expect(wrapper.exists()).toBeTruthy()
  })
})
